package com.dpsk.dpsk_quiz_sys_java.service;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import net.sourceforge.tess4j.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class TessOcrService {

    private final Tesseract tesseract;

    public TessOcrService(Tesseract tesseract) {
        this.tesseract = tesseract;
    }

    /**
     * 高精度处理PDF文件
     */
    public String processPdfWithHighAccuracy(File pdfFile) throws IOException, TesseractException {
        List<BufferedImage> images = pdfToImages(pdfFile, 600);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < images.size(); i++) {
            BufferedImage image = images.get(i);
            // 预处理图像
            BufferedImage processedImage = preprocessImage(image);

            // 分块识别提高准确率
            String pageText = ocrImageWithBlocks(processedImage);

            result.append(String.format("=== 第 %d 页 ===\n", i + 1))
                    .append(pageText)
                    .append("\n\n");
        }

        return result.toString();
    }

    /**
     * 处理图片压缩包（支持ZIP/RAR）
     */
    public List<String> processImageArchive(MultipartFile archiveFile)
            throws IOException, TesseractException, RarException {

        List<String> results = new ArrayList<>();
        String extension = FilenameUtils.getExtension(archiveFile.getOriginalFilename())
                .toLowerCase();

        try (InputStream is = archiveFile.getInputStream()) {
            switch (extension) {
                case "zip":
                    processZipFile(is, results);
                    break;
                case "rar":
                    processRarFile(is, results);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的压缩格式: " + extension);
            }
        }

        return results;
    }

    // 处理ZIP文件
    private void processZipFile(InputStream is, List<String> results)
            throws IOException, TesseractException {

        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && isImageFile(entry.getName())) {
                    BufferedImage image = ImageIO.read(zis);
                    if (image != null) {
                        results.add(ocrImageWithEnhancement(image));
                    }
                }
                zis.closeEntry();
            }
        }
    }

    // 处理RAR文件
    private void processRarFile(InputStream is, List<String> results)
            throws IOException, TesseractException, RarException {

        // 需要将InputStream写入临时文件（junrar需要File形式）
        Path tempFile = Files.createTempFile("temp_", ".rar");
        try {
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

            try (Archive archive = new Archive(tempFile.toFile())) {
                FileHeader fh;
                while ((fh = archive.nextFileHeader()) != null) {
                    if (!fh.isDirectory() && isImageFile(fh.getFileName())) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        archive.extractFile(fh, os);

                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(os.toByteArray()));
                        if (image != null) {
                            results.add(ocrImageWithEnhancement(image));
                        }
                    }
                }
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }




    /**
     * 增强型图片OCR识别
     */
    public String ocrImageWithEnhancement(MultipartFile imageFile) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageFile.getBytes()));
        return ocrImageWithEnhancement(image);
    }

    /**
     * 增强型图片OCR识别
     */
    public String ocrImageWithEnhancement(BufferedImage image) throws TesseractException {
        BufferedImage processedImage = preprocessImage(image);
        return tesseract.doOCR(processedImage);
    }

    /**
     * 分块识别图像（提高大图像识别率）
     */
    private String ocrImageWithBlocks(BufferedImage image) throws TesseractException {
        // 如果图像太大，分割成小块识别
        if (image.getWidth() > 2000 || image.getHeight() > 2000) {
            return processImageByBlocks(image, 1000, 1000);
        }
        return tesseract.doOCR(image);
    }

    /**
     * 图像预处理
     */
    private BufferedImage preprocessImage(BufferedImage original) {
        // 转换为灰度图
        BufferedImage grayscale = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics g = grayscale.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();

        // 二值化
        BufferedImage binary = new BufferedImage(
                grayscale.getWidth(),
                grayscale.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);

        Graphics2D g2d = binary.createGraphics();
        g2d.drawImage(grayscale, 0, 0, null);
        g2d.dispose();

        // 锐化
        BufferedImage sharpened = sharpenImage(binary);

        return sharpened;
    }

    /**
     * 图像锐化处理
     */
    private BufferedImage sharpenImage(BufferedImage image) {
        Kernel kernel = new Kernel(3, 3,
                new float[] {
                        -1, -1, -1,
                        -1,  9, -1,
                        -1, -1, -1
                });

        ConvolveOp op = new ConvolveOp(kernel);
        return op.filter(image, null);
    }

    /**
     * 分块处理大图像
     */
    private String processImageByBlocks(BufferedImage image, int blockWidth, int blockHeight)
            throws TesseractException {

        StringBuilder result = new StringBuilder();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y += blockHeight) {
            for (int x = 0; x < width; x += blockWidth) {
                int w = Math.min(blockWidth, width - x);
                int h = Math.min(blockHeight, height - y);

                BufferedImage subImage = image.getSubimage(x, y, w, h);
                String text = tesseract.doOCR(subImage);
                result.append(text).append(" ");
            }
        }

        return result.toString();
    }

    /**
     * PDF转图片（带更多优化参数）
     */
    public List<BufferedImage> pdfToImages(File pdfFile, int dpi) throws IOException {
        List<BufferedImage> images = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(true); // 允许子采样

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                // 使用抗锯齿渲染
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                images.add(image);
            }
        }

        return images;
    }

    private boolean isImageFile(String filename) {
        String ext = FilenameUtils.getExtension(filename).toLowerCase();
        return Set.of("jpg", "jpeg", "png", "bmp", "gif").contains(ext);
    }
}

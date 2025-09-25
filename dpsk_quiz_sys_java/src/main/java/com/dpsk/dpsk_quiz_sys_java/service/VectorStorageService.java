package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.KbChunk;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量存储服务（简化版本）
 * 实际项目中应使用专门的向量数据库如ChromaDB、Pinecone等
 */
@Service
public class VectorStorageService {
    
    // 简化的向量存储，实际应使用专门的向量数据库
    private final Map<Long, float[]> chunkVectors = new HashMap<>();
    private final Map<Long, KbChunk> chunkData = new HashMap<>();
    
    /**
     * 为知识块生成向量并存储
     * @param chunk 知识块
     */
    public void storeChunkVector(KbChunk chunk) {
        try {
            // 生成文本向量（简化版本）
            float[] vector = generateTextVector(chunk.getContent());
            
            // 存储向量和数据
            chunkVectors.put(chunk.getChunkId(), vector);
            chunkData.put(chunk.getChunkId(), chunk);
            
            System.out.println("已为知识块 " + chunk.getChunkId() + " 生成并存储向量，维度: " + vector.length);
            
        } catch (Exception e) {
            System.err.println("向量生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 批量存储知识块向量
     * @param chunks 知识块列表
     */
    public void storeChunkVectors(List<KbChunk> chunks) {
        for (KbChunk chunk : chunks) {
            storeChunkVector(chunk);
        }
    }
    
    /**
     * 语义搜索：根据查询文本找到最相似的知识块
     * @param queryText 查询文本
     * @param topK 返回最相似的K个结果
     * @return 相似的知识块列表
     */
    public List<KbChunk> semanticSearch(String queryText, int topK) {
        try {
            // 生成查询向量
            float[] queryVector = generateTextVector(queryText);
            
            // 计算相似度并排序
            List<SimilarityResult> results = new ArrayList<>();
            
            for (Map.Entry<Long, float[]> entry : chunkVectors.entrySet()) {
                Long chunkId = entry.getKey();
                float[] chunkVector = entry.getValue();
                
                // 计算余弦相似度
                double similarity = calculateCosineSimilarity(queryVector, chunkVector);
                
                KbChunk chunk = chunkData.get(chunkId);
                if (chunk != null) {
                    results.add(new SimilarityResult(chunk, similarity));
                }
            }
            
            // 按相似度降序排序并返回topK结果
            return results.stream()
                    .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                    .limit(topK)
                    .map(result -> result.chunk)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("语义搜索失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 生成文本向量（简化版本）
     * 实际项目中应使用Deepseek-1024d或其他专业模型
     * @param text 文本内容
     * @return 向量数组
     */
    private float[] generateTextVector(String text) {
        if (text == null || text.isEmpty()) {
            return new float[1024]; // 返回零向量
        }
        
        // 简化的向量生成：基于文本特征的哈希向量
        int vectorDim = 1024; // 模拟Deepseek-1024d的维度
        float[] vector = new float[vectorDim];
        
        // 使用文本内容生成特征向量
        String cleanText = text.toLowerCase().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", "");
        char[] chars = cleanText.toCharArray();
        
        // 基于字符和位置生成向量特征
        for (int i = 0; i < chars.length && i < vectorDim; i++) {
            int charCode = (int) chars[i];
            int index = (charCode * (i + 1)) % vectorDim;
            vector[index] += 1.0f / (i + 1); // 位置权重递减
        }
        
        // 基于词频生成特征
        String[] words = cleanText.split("\\s+");
        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            if (word.length() > 1) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        
        // 将词频信息编码到向量中
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            int hash = Math.abs(word.hashCode()) % vectorDim;
            vector[hash] += count * 0.1f;
        }
        
        // 向量归一化
        normalizeVector(vector);
        
        return vector;
    }
    
    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    /**
     * 向量归一化
     */
    private void normalizeVector(float[] vector) {
        double norm = 0.0;
        for (float v : vector) {
            norm += v * v;
        }
        
        if (norm > 0) {
            norm = Math.sqrt(norm);
            for (int i = 0; i < vector.length; i++) {
                vector[i] = (float) (vector[i] / norm);
            }
        }
    }
    
    /**
     * 删除知识块的向量数据
     */
    public void deleteChunkVector(Long chunkId) {
        chunkVectors.remove(chunkId);
        chunkData.remove(chunkId);
        System.out.println("已删除知识块 " + chunkId + " 的向量数据");
    }
    
    /**
     * 获取存储的向量数量
     */
    public int getVectorCount() {
        return chunkVectors.size();
    }
    
    /**
     * 相似度结果内部类
     */
    private static class SimilarityResult {
        final KbChunk chunk;
        final double similarity;
        
        SimilarityResult(KbChunk chunk, double similarity) {
            this.chunk = chunk;
            this.similarity = similarity;
        }
    }
}
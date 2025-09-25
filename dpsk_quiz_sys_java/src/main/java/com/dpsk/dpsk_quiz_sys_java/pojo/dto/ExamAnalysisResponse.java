package com.dpsk.dpsk_quiz_sys_java.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamAnalysisResponse {
    private BigDecimal totalScore;
    private Integer duration;
    private List<QuestionTypeAccuracy> questionTypes;
    
    // getters & setters
    
    public static class QuestionTypeAccuracy {
        private String typeName;
        private String accuracy;
        // getters & setters
        public QuestionTypeAccuracy(String typeName, String accuracy) {
            this.typeName = typeName;
            this.accuracy = accuracy;
        }
        //getter
        public String getTypeName() {
            return typeName;
        }
        //getter
        public String getAccuracy() {
            return accuracy;
        }
        //setter
        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
        //setter
        public void setAccuracy(String accuracy) {
            this.accuracy = accuracy;
        }
        //toString
        @Override
        public String toString() {
            return "QuestionTypeAccuracy{" +
                    "typeName='" + typeName + '\'' +
                    ", accuracy='" + accuracy + '\'' +
                    '}';
        }
    }
}
package com.yuki.Utils;

public class MatrixOperationsUtils {
    public static int[][] addMatrices(int[][] matrixA, int[][] matrixB) {
        int rows = matrixA.length;
        int columns = matrixA[0].length;
        int[][] result = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrixA[i][j] + matrixB[i][j];
            }
        }
        return result;
    }

    public static int[][] subtractMatrices(int[][] matrixA, int[][] matrixB) {
        // ... 类似于矩阵加法 ...
        int rows = matrixA.length;
        int columns = matrixA[0].length;
        int[][] result = new int[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = matrixA[i][j] - matrixB[i][j];
            }
        }
        return result;
    }

    public static int[][] multiplyMatrices(int[][] matrixA, int[][] matrixB) {
        int aRows = matrixA.length; // 这会给出矩阵的行数
        int aColumns = matrixA[0].length; // 这会给出矩阵的列数
        int bRows = matrixB.length;
        int bColumns = matrixB[0].length;

        if (aColumns != bRows) {
            throw new IllegalArgumentException("矩阵不可乘");
        }

        int[][] result = new int[aRows][bColumns];

        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bColumns; j++) {
                for (int k = 0; k < aColumns; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];// 注意第几行第几列
                }
            }
        }
        return result;
    }

    public static Double cosineSimilarity(Double[] vectorA, Double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
//            计算分子
            dotProduct += vectorA[i] * vectorB[i];
//            计算分母
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}

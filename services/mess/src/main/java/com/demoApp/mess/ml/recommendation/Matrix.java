package com.demoApp.mess.ml.recommendation;

import java.util.Arrays;

public class Matrix {
    private double[][] data;
    private int rows;
    private int cols;
    
    /**
     * Creates a new matrix with the specified dimensions.
     * @param rows Number of rows
     * @param cols Number of columns
     */
    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }
    
    /**
     * Creates a matrix from a 2D array of values.
     * @param data The 2D array to initialize the matrix with
     */
    public Matrix(double[][] data) {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            if (data[i].length != cols) {
                throw new IllegalArgumentException("All rows must have the same number of columns");
            }
            System.arraycopy(data[i], 0, this.data[i], 0, cols);
        }
    }
    
    /**
     * Gets the value at the specified position.
     * @param row Row index
     * @param col Column index
     * @return The value at the specified position
     */
    public double get(int row, int col) {
        validateIndices(row, col);
        return data[row][col];
    }
    
    /**
     * Sets the value at the specified position.
     * @param row Row index
     * @param col Column index
     * @param value The value to set
     */
    public void set(int row, int col, double value) {
        validateIndices(row, col);
        data[row][col] = value;
    }
    
    /**
     * Gets the number of rows in the matrix.
     * @return The number of rows
     */
    public int getRows() {
        return rows;
    }
    
    /**
     * Gets the number of columns in the matrix.
     * @return The number of columns
     */
    public int getCols() {
        return cols;
    }
    
    /**
     * Adds this matrix to another matrix.
     * @param other The matrix to add
     * @return A new matrix representing the sum
     */
    public Matrix add(Matrix other) {
        if (rows != other.rows || cols != other.cols) {
            throw new IllegalArgumentException("Matrices must have the same dimensions for addition");
        }
        
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        
        return result;
    }
    
    /**
     * Multiplies this matrix by another matrix.
     * @param other The matrix to multiply with
     * @return A new matrix representing the product
     */
    public Matrix multiply(Matrix other) {
        if (cols != other.rows) {
            throw new IllegalArgumentException("Number of columns in first matrix must equal number of rows in second matrix");
        }
        
        Matrix result = new Matrix(rows, other.cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                double sum = 0;
                for (int k = 0; k < cols; k++) {
                    sum += data[i][k] * other.data[k][j];
                }
                result.data[i][j] = sum;
            }
        }
        
        return result;
    }
    
    /**
     * Calculates the transpose of this matrix.
     * @return A new matrix representing the transpose
     */
    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[j][i] = this.data[i][j];
            }
        }
        
        return result;
    }
    
    /**
     * Validates that the provided indices are within bounds.
     * @param row Row index
     * @param col Column index
     */
    private void validateIndices(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Indices out of range: [" + row + ", " + col + "]");
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append(Arrays.toString(data[i]));
            if (i < rows - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

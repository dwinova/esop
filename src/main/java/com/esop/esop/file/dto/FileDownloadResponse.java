package com.esop.esop.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadResponse {
    private String filename;
    private String contentType;
    private byte[] data;
}

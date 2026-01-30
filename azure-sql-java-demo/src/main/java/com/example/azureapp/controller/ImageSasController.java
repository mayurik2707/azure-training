package com.example.azureapp.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.azureapp.service.BlobSasService;

@Controller
public class ImageSasController {

    private final BlobSasService sasService;

    public ImageSasController(BlobSasService sasService) {
        this.sasService = sasService;
    }

    // Return the SAS URL as plain text (frontend sets <img>.src to this)
    @GetMapping("/image/url")
    @ResponseBody
    public ResponseEntity<String> getImageUrl() {
        // Choose the appropriate method based on your environment/config:
        // return ResponseEntity.ok(sasService.generateReadOnlySasUrlWithSharedKey());
        return ResponseEntity.ok(sasService.generateReadOnlySasUrlWithSharedKey());
    }
}
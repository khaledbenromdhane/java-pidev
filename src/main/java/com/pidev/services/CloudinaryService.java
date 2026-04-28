package com.pidev.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryService {
    private static CloudinaryService instance;
    private final Cloudinary cloudinary;
    private final String uploadPreset = "publication";

    private CloudinaryService() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "djotvqu3z",
            "api_key", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
            "api_secret", "xxxxxxxxxxxxxxxxx",
            "secure", true
        ));
    }




    

    public static synchronized CloudinaryService getInstance() {
        if (instance == null) {
            instance = new CloudinaryService();
        }
        return instance;
    }

    /**
     * Uploads an image file to Cloudinary using an unsigned upload preset.
     * @param file The image file to upload.
     * @return The URL of the uploaded image, or null if upload fails.
     */
    public String uploadImage(File file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "unsigned", true,
                "upload_preset", uploadPreset
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            System.err.println("Cloudinary Upload Error: " + e.getMessage());
            return null;
        }
    }
}

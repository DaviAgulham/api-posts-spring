package com.example.api_spring.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorage {
  private final Path root;

  public FileStorage(@Value("${app.upload-dir}") String dir) throws IOException {
    this.root = Paths.get(dir).toAbsolutePath();
    Files.createDirectories(root);
  }

  public String save(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) return null;
    String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
    String name = UUID.randomUUID().toString() + (ext != null ? "." + ext.toLowerCase() : "");
    Files.copy(file.getInputStream(), root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
    return "/uploads/" + name;
  }
}

package com.example.api_spring.post;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.api_spring.user.User;
import com.example.api_spring.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PostService {
  private final PostRepository repo;
  private final UserRepository users;

  public PostService(PostRepository repo, UserRepository users) {
    this.repo = repo; this.users = users;
  }

  public List<Post> listAll() { return repo.findAll(); }

  public Post create(User owner, String title, String description, MultipartFile image) {
    Post p = new Post();
    p.setUser(owner);
    p.setTitle(title);
    p.setDescription(description);
    if (image != null && !image.isEmpty()) p.setImagePath(saveImage(image));
    return repo.save(p);
  }

  public Post get(Long id) {
    return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Post not found"));
  }

  public Post update(User auth, Long id, String title, String description, MultipartFile image) {
    Post p = get(id);
    boolean admin = auth.isAdmin();
    boolean owner = p.getUser().getId().equals(auth.getId());
    if (!admin && !owner) throw new AccessDeniedException("Forbidden");

    if (title != null) p.setTitle(title);
    if (description != null) p.setDescription(description);
    if (image != null && !image.isEmpty()) {
      if (p.getImagePath() != null) deleteImage(p.getImagePath());
      p.setImagePath(saveImage(image));
    }
    return repo.save(p);
  }

  public void delete(User auth, Long id) {
    Post p = get(id);
    boolean admin = auth.isAdmin();
    boolean owner = p.getUser().getId().equals(auth.getId());
    if (!admin && !owner) throw new AccessDeniedException("Forbidden");
    if (p.getImagePath() != null) deleteImage(p.getImagePath());
    repo.delete(p);
  }

  private String saveImage(MultipartFile file) {
    try {
      Path dir = Path.of("uploads", "posts");
      Files.createDirectories(dir);
      String fname = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+","_");
      Path path = dir.resolve(fname);
      file.transferTo(path);
      return "posts/" + fname;
    } catch (Exception e) { throw new RuntimeException("Image upload failed"); }
  }
  private void deleteImage(String relPath) { try { Files.deleteIfExists(Path.of("uploads", relPath)); } catch (Exception ignored) {} }
}

// src/main/java/com/example/api_spring/post/PostController.java
package com.example.api_spring.post;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.api_spring.files.FileStorage;
import com.example.api_spring.user.User;
import com.example.api_spring.user.UserRepository;

@RestController
@RequestMapping("/api/posts")
public class PostController {
  private final PostRepository posts;
  private final UserRepository users;
  private final FileStorage files;

  public PostController(PostRepository posts, UserRepository users, FileStorage files) {
    this.posts = posts; this.users = users; this.files = files;
  }


  @GetMapping
  public List<Post> list() { return posts.findAllByOrderByCreatedAtDesc(); }

  @GetMapping("/{id}")
  public Post one(@PathVariable Long id) {
    Post p = posts.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    p.getUser().getId();
    return p;
  }


  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Post createMultipart(
    @AuthenticationPrincipal User me,
    @RequestParam String title,
    @RequestParam String description,
    @RequestPart(value = "image", required = false) MultipartFile image,
    @RequestPart(value = "file",   required = false) MultipartFile file
  ) throws Exception {
    MultipartFile f = (image != null && !image.isEmpty()) ? image : file;
    String imagePath = (f != null && !f.isEmpty()) ? files.save(f) : null;
    Post p = Post.builder().user(me).title(title).description(description).imagePath(imagePath).build();
    return posts.save(p);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public Post createJson(@AuthenticationPrincipal User me, @RequestBody PostCreateDto dto) {
    Post p = Post.builder()
        .user(me).title(dto.title()).description(dto.description()).build();
    return posts.save(p);
  }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post updateMultipart(
        @PathVariable Long id,
        @AuthenticationPrincipal User me,
        @RequestParam(required=false) String title,
        @RequestParam(required=false) String description,
        @RequestPart(value = "image", required = false) MultipartFile image,
        @RequestPart(value = "file",   required = false) MultipartFile file
    ) throws Exception {
      Post p = posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      enforceOwnerOrAdmin(p, me);
      if (title != null) p.setTitle(title);
      if (description != null) p.setDescription(description);
      MultipartFile f = (image != null && !image.isEmpty()) ? image : file;
      if (f != null && !f.isEmpty()) p.setImagePath(files.save(f));
      return posts.save(p);
    }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Post updateJson(@PathVariable Long id, @AuthenticationPrincipal User me, @RequestBody PostCreateDto dto) {
    Post p = posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    enforceOwnerOrAdmin(p, me);
    p.setTitle(dto.title());
    p.setDescription(dto.description());
    return posts.save(p);
  }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Post updateMultipartPost(@PathVariable Long id, @AuthenticationPrincipal User me,
        @RequestParam(required=false) String title,
        @RequestParam(required=false) String description,
        @RequestPart(value="image", required=false) MultipartFile image,
        @RequestPart(value="file",   required=false) MultipartFile file) throws Exception {
      return updateMultipart(id, me, title, description, image, file);
    }

  @PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Post updateJsonPost(@PathVariable Long id, @AuthenticationPrincipal User me, @RequestBody PostCreateDto dto) {
    return updateJson(id, me, dto);
  }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable Long id,
        @AuthenticationPrincipal User me
    ) {
      Post p = posts.findById(id)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
      enforceOwnerOrAdmin(p, me);
      posts.delete(p);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteViaPost(
        @PathVariable Long id,
        @AuthenticationPrincipal User me,
        @RequestParam(value = "_method", required = false) String method
    ) {
    if (!"DELETE".equalsIgnoreCase(method))
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED);
    delete(id, me);
    }

  private void enforceOwnerOrAdmin(Post p, User me) {
    if (!me.isAdmin() && !p.getUser().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  public record PostCreateDto(String title, String description) {}
}

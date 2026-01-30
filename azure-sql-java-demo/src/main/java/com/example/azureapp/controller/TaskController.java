package com.example.azureapp.controller;

import com.example.azureapp.model.Task;
import com.example.azureapp.repository.TaskRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaskController {

    private final TaskRepository repo;

    public TaskController(TaskRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", repo.findAll());
        return "index";
    }

    @PostMapping("/add")
    public String add(@RequestParam String title) {
        if (title != null && !title.isBlank()) {
            repo.save(new Task(title.trim()));
        }
        return "redirect:/";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        repo.findById(id).ifPresent(t -> { t.setDone(!t.isDone()); repo.save(t); });
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/";
    }
}
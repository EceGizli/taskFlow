package com.beat.taskFlow.project;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public Project updateProject(Long id, Project updatedProject) {

        Project project = getProjectById(id);

        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());

        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
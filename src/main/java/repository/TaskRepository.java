package main.java.repository;

import main.java.exceptions.EpicNotFoundException;
import main.java.exceptions.TaskNotFoundException;
import main.java.tasks.Epic;
import main.java.tasks.SubTask;
import main.java.tasks.Task;
import main.java.tasks.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TaskRepository {
    private final HashMap<Integer, Task> allTasks;

    public TaskRepository() {
        this.allTasks = new HashMap<>();
    }

    public void saveTask(Task task) {
        allTasks.put(task.getId(), task);
    }

    public void addSubTaskInEpic(int epicId, SubTask subTask) {
        Epic epic = (Epic) allTasks.get(epicId);
        epic.addSubTask(subTask.getId(), subTask);
    }

    public Task getTaskById(int id) throws TaskNotFoundException {
        if (!allTasks.containsKey(id)) {
            throw new TaskNotFoundException(id);
        } else {
            return allTasks.get(id);
        }
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    public List<Task> getAllSingleTasks() {
        return allTasks.values()
                .stream()
                .filter(task -> (task.getType() == Type.SINGLE))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Task> getAllEpic() {
        return allTasks.values()
                .stream()
                .filter(task -> task.getType() == Type.EPIC)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Task> getAllSubTasks() {
        return allTasks.values()
                .stream()
                .filter(task -> task.getType() == Type.SUB)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<SubTask> getAllSubTasksByEpicId(int id) throws EpicNotFoundException {
        if (!(allTasks.get(id) instanceof Epic)) {
            throw new EpicNotFoundException(id);
        }

        Epic epic = (Epic) allTasks.get(id);

        return epic.getSubTasks();
    }

    public Task updateTask(Task task) throws TaskNotFoundException {
        int taskId = task.getId();

        if (!allTasks.containsKey(taskId)) {
            throw new TaskNotFoundException(taskId);
        } else {
            if (allTasks.get(task.getId()) instanceof SubTask) {
                updateListSubTasks((SubTask) allTasks.get(taskId), (SubTask) task);
            }
            allTasks.put(taskId, task);
            return task;
        }
    }

    private void updateListSubTasks(SubTask oldSubTask, SubTask newSubTask) {
        Epic epic = (Epic) allTasks.get(newSubTask.getEpicId());

        if (!isEpicIdEquals(oldSubTask, newSubTask)) {
            removeOldSubTask(oldSubTask);
        }

        epic.addSubTask(newSubTask.getId(), newSubTask);
    }

    private boolean isEpicIdEquals(SubTask oldTask, SubTask newTask) {
        return oldTask.getEpicId() == newTask.getEpicId();
    }

    private void removeOldSubTask(SubTask oldSubTask) {
        Epic oldEpic = (Epic) allTasks.get(oldSubTask.getEpicId());
        oldEpic.removeSubTask(oldSubTask.getId());
    }

    public void removeAllTasks() {
        allTasks.clear();
    }

    public void removeTaskById(int id) throws TaskNotFoundException {
        if (!allTasks.containsKey(id)) {
            throw new TaskNotFoundException(id);
        } else {
            if (allTasks.get(id) instanceof SubTask) {
                removeSubTaskFromEpic(id);
            } else if (allTasks.get(id) instanceof Epic) {
                changeTypeSubTasks((Epic) allTasks.get(id));
            }
            allTasks.remove(id);
        }
    }

    private void removeSubTaskFromEpic(int id) {
        SubTask subTask = (SubTask) allTasks.get(id);
        Epic epic = (Epic) allTasks.get(subTask.getEpicId());
        epic.removeSubTask(id);
    }

    private void changeTypeSubTasks(Epic epic) {
        for (SubTask subTask : epic.getSubTasks()) {
            allTasks.put(subTask.getId(), new Task(
                    subTask.getId(),
                    subTask.getName(),
                    subTask.getDescription(),
                    subTask.getStatus())
            );
        }

        epic.removeAllSubTask();
    }
}

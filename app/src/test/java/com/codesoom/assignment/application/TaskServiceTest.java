package com.codesoom.assignment.application;

import com.codesoom.assignment.exceptions.TaskDuplicationException;
import com.codesoom.assignment.exceptions.TaskNotFoundException;
import com.codesoom.assignment.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TaskService 클래스")
class TaskServiceTest {
    private static final Long TEST_ID = 1L;
    private static final Long TEST_NOT_EXSIT_ID = 100L;
    private static final String TEST_TITLE = "테스트는 재밌군요!";
    private static final String POSTFIX_TITLE = " 그치만 매우 생소하군요!";
    private static final String FIVE_HOURS_TITLE = "5시간 후까지 해야되는 작업!!";
    private static final String ONE_DAY_TITLE = "1일 후까지 해야되는 작업!!";
    private static final String TWO_WEEKS_TITLE = "2주 후까지 해야되는 작업!!";
    private TaskService taskService;
    private Task taskSource;

    /**
     * 각 테스트에서 필요한 fixture 데이터를 생성합니다.
     */
    @BeforeEach
    void setUp() {
        taskService = new TaskService();
        taskSource = Task.builder()
                .title(TEST_TITLE)
                .build();

        taskService.createTask(taskSource);
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class getTasks_메서드는 {

        @BeforeEach
        void setUp() { // fixture 데이터 리셋
            taskService = new TaskService();
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 할_일이_없을_때 {
            @Test
            @DisplayName("빈 ArrayList를 리턴한다")
            void it_returns_empty_array() {
                List<Task> tasks = taskService.getTasks();

                assertThat(tasks)
                        .isInstanceOf(ArrayList.class)
                        .isEmpty();
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 할_일이_있을_때 {
            @DisplayName("n개의 할 일 목록을 리턴한다")
            @ParameterizedTest(name = "{arguments}개의 할 일 목록을 리턴한다")
            @ValueSource(ints = {1, 77, 1027})
            void it_returns_tasks(int createCount) {
                createTaskUntilCount(createCount);

                List<Task> tasks = taskService.getTasks();

                assertThat(tasks)
                        .withFailMessage("%d개의 할 일을 리턴해야합니다", createCount)
                        .hasSize(createCount);
            }

            @Test
            @DisplayName("종료 시간이 임박한 순으로 할 일 목록을 리턴한다")
            void it_returns_tasks() {
                createTaskWithEndtime();

                List<Task> tasks = taskService.getTasks();

                assertThat(tasks.get(0).getTitle())
                        .isEqualTo(FIVE_HOURS_TITLE);

                assertThat(tasks.get(1).getTitle())
                        .isEqualTo(ONE_DAY_TITLE);

                assertThat(tasks.get(2).getTitle())
                        .isEqualTo(TWO_WEEKS_TITLE);
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class getTask_메서드는 {

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 찾을_수_있는_id가_주어지면 {
            @Test
            @DisplayName("해당 id의 할 일을 리턴한다")
            void it_returns_task() {
                Task task = taskService.getTask(TEST_ID);

                assertThat(task).isNotNull();
                assertThat(task.getId()).isEqualTo(TEST_ID);
                assertThat(task.getTitle()).isEqualTo(TEST_TITLE);
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 찾을_수_없는_id가_주어지면 {
            @Test
            @DisplayName("예외를 던진다")
            void it_returns_taskNotFoundException() {
                assertThatThrownBy(() -> taskService.getTask(TEST_NOT_EXSIT_ID))
                        .isInstanceOf(TaskNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class createTask_메서드는 {

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 새로운_할_일이_주어지면 {

            @Nested
            @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
            class 중복된_할_일인_경우 {
                @Test
                @DisplayName("예외를 던진다")
                void it_returns_taskAlreadyExistException() {
                    assertThatThrownBy(() -> taskService.createTask(taskSource))
                            .isInstanceOf(TaskDuplicationException.class);
                }

                @Test
                @DisplayName("예외 메시지를 \"이미 등록된 할 일입니다\" 라는 문구로 리턴한다")
                void it_returns_exception_message() {
                    assertThatThrownBy(() -> taskService.createTask(taskSource))
                            .hasMessage("이미 등록된 할 일입니다");
                }
            }

            @Nested
            @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
            class 중복된_할_일이_아닌_경우 {
                private Task createTaskSource;

                @BeforeEach
                void setUp() {
                    createTaskSource = Task.builder()
                            .title(POSTFIX_TITLE)
                            .build();
                }

                @Test
                @DisplayName("할 일이 하나 늘어난다")
                void it_returns_tasks_size() {
                    int oldSize = taskService.getTasks().size();

                    taskService.createTask(createTaskSource);

                    int newSize = taskService.getTasks().size();

                    assertThat(newSize - oldSize).isEqualTo(1);
                }

                @Test
                @DisplayName("할 일의 id 값은 유니크한 값으로 저장된다")
                void it_returns_id_count_one() {
                    Task task = taskService.createTask(createTaskSource);
                    Long newId = task.getId();

                    List<Task> tasks = taskService.getTasks();

                    long idCount = tasks.stream()
                            .map(Task::getId)
                            .filter(id -> id.equals(newId))
                            .count();

                    assertThat(idCount).isEqualTo(1);
                }

                @Nested
                @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
                class 종료_시간_설정을_안했다면 {
                    @Test
                    @DisplayName("할 일을 저장하고 기본 종료시간을 포함하여 리턴한다")
                    void it_return_created_task() {
                        Task task = taskService.createTask(createTaskSource);

                        assertThat(task).isNotNull();
                        assertThat(task.getTitle()).isEqualTo(POSTFIX_TITLE);
                        assertThat(task.getEndTime()).isEqualTo(LocalDateTime.MAX);
                    }
                }

                @Nested
                @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
                class 종료_시간_설정을_했다면 {
                    @Test
                    @DisplayName("할 일을 저장하고 설정한 종료시간을 포함하여 리턴한다")
                    void it_return_created_task() {
                        LocalDateTime endTime = LocalDateTime.now().plusDays(1L);

                        Task createTaskSource = Task.builder()
                                .title(POSTFIX_TITLE)
                                .endTime(endTime)
                                .build();

                        Task task = taskService.createTask(createTaskSource);

                        assertThat(task).isNotNull();
                        assertThat(task.getTitle()).isEqualTo(POSTFIX_TITLE);
                        assertThat(task.getEndTime()).isEqualTo(endTime);
                    }
                }
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class null이_주어지면 {
            @Test
            @DisplayName("예외를 던진다")
            void it_returns_NullPointerException() {
                assertThatThrownBy(() -> taskService.createTask(null))
                        .isInstanceOf(NullPointerException.class);
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class updateTask_메서드는 {

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 찾을_수_있는_id가_주어지면 {
            @Test
            @DisplayName("할 일을 수정하고 리턴한다")
            void it_returns_updated_task() {
                LocalDateTime time = LocalDateTime.now();

                Task source = Task.builder()
                        .title(TEST_TITLE + POSTFIX_TITLE)
                        .endTime(time)
                        .build();

                Task task = taskService.updateTask(TEST_ID, source);

                assertThat(task).isNotNull();
                assertThat(task.getTitle()).isEqualTo(TEST_TITLE + POSTFIX_TITLE);
                assertThat(task.getEndTime()).isEqualTo(time);
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 찾을_수_없는_id가_주어지면 {
            @Test
            @DisplayName("예외를 던진다")
            void it_returns_taskNotFoundException() {
                assertThatThrownBy(() -> taskService.updateTask(TEST_NOT_EXSIT_ID, taskSource))
                        .isInstanceOf(TaskNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class deleteTask_메서드는 {

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 찾을_수_있는_id가_주어지면 {
            @Test
            @DisplayName("할 일을 삭제하고 리턴한다")
            void it_returns_deleted_task() {
                Task task = taskService.deleteTask(TEST_ID);

                assertThat(task).isNotNull();
                assertThat(task.getId()).isEqualTo(TEST_ID);
                assertThat(task.getTitle()).isEqualTo(TEST_TITLE);
            }

            @Test
            @DisplayName("할 일이 하나 줄어든다")
            void it_returns_tasks_size() {
                int oldSize = taskService.getTasks().size();

                taskService.deleteTask(TEST_ID);

                int newSize = taskService.getTasks().size();

                assertThat(newSize - oldSize).isEqualTo(-1);
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
        class 찾을_수_없는_id가_주어지면 {
            @Test
            @DisplayName("예외를 던진다")
            void it_returns_taskNotFoundException() {
                assertThatThrownBy(() -> taskService.deleteTask(TEST_NOT_EXSIT_ID))
                        .isInstanceOf(TaskNotFoundException.class);
            }
        }
    }


    private void createTaskUntilCount(int createCount) {
        for (int i = 0; i < createCount; i++) {
            Task tempCreateTask = Task.builder()
                    .title(createCount + " " + i)
                    .build();

            taskService.createTask(tempCreateTask);
        }
    }

    private void createTaskWithEndtime() {
        Task endTimeAfter5Hour = Task.builder()
                .title(FIVE_HOURS_TITLE)
                .endTime(LocalDateTime.now().plusHours(5L))
                .build();

        Task endTimeAfter1Day = Task.builder()
                .title(ONE_DAY_TITLE)
                .endTime(LocalDateTime.now().plusDays(1L))
                .build();

        Task endTimeAfter2WeekDay = Task.builder()
                .title(TWO_WEEKS_TITLE)
                .endTime(LocalDateTime.now().plusWeeks(2L))
                .build();

        taskService.createTask(endTimeAfter2WeekDay);
        taskService.createTask(endTimeAfter1Day);
        taskService.createTask(endTimeAfter5Hour);
    }
}

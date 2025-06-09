#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
#include <unistd.h>
#include <string.h>

void kill_process(pid_t pid)
{
	if (kill(pid, SIGTERM) == -1)
	{
		perror("Failed to kill process");
	}
}

/* the program execution starts here */
int main(int argc, char **argv)
{
	char *program;
	pid_t pid, procmon_pid, filter_pid;
	int fd[2];

	if (argc != 2)
	{
		printf("Usage: mon fileName\n where fileName is an executable file.\n");
		exit(-1);
	}
	else
	{
		program = argv[1];

		// Create pipe to pass messages
		if (pipe(fd) == -1)
		{
			fprintf(stderr, "Pipe failed");
			return -1;
		}

		// Fork a new process for program
		pid = fork();
		if (pid < 0)
		{
			fprintf(stderr, "Fork failed\n");
			return 1;
		}
		else if (pid == 0)
		{
			// Execute program
			execl(program, program, (char *)NULL);
			perror("execl failed\n");
			exit(EXIT_FAILURE);
		}

		sleep(1);

		// Get string of program's pid to pass as cmd arg
		char pid_str[10];
		sprintf(pid_str, "%d", pid);

		// Fork new process for procmon
		procmon_pid = fork();
		if (procmon_pid < 0)
		{
			fprintf(stderr, "Fork failed");
			kill_process(pid);
			return 1;
		}
		else if (procmon_pid == 0)
		{
			close(fd[0]);				// Close read end of pipe
			dup2(fd[1], STDOUT_FILENO); // Redirect output of process to write end of pipe
			close(fd[1]);				// Close write end of pipe

			// Execute procmon with pid_str as arg
			execl("./procmon", "procmon", pid_str, (char *)NULL);
			perror("execl for procmon failed\n");
			exit(EXIT_FAILURE);
		}

		// Fork new process for filter
		filter_pid = fork();
		if (filter_pid < 0)
		{
			fprintf(stderr, "Fork failed\n");
			kill_process(pid);
			kill_process(procmon_pid);
			return 1;
		}
		else if (filter_pid == 0)
		{
			close(fd[1]);			   // Close write end of pipe
			dup2(fd[0], STDIN_FILENO); // Redirect read end of pipe to process input
			close(fd[0]);			   // Close read end of pipe

			// Execute filter
			execl("./filter", "filter", (char *)NULL);
			perror("execl for filter failed\n");
			exit(EXIT_FAILURE);
		}

		// Close both ends of pipe
		close(fd[1]);
		close(fd[0]);

		sleep(20);
		kill_process(pid);
		sleep(2);
		kill_process(procmon_pid);
		kill_process(filter_pid);
		waitpid(pid, NULL, 0);
		waitpid(procmon_pid, NULL, 0);
		waitpid(filter_pid, NULL, 0);
	}

	return 0;
}

#ifndef LLAMA_RUNNER_H
#define LLAMA_RUNNER_H

#ifdef __cplusplus
extern "C" {
#endif

void llama_runner_init(void);
int  llama_runner_load_model(const char* model_path);
char* llama_runner_generate_text(const char* prompt, int max_tokens);
void llama_runner_unload_model(void);
void llama_runner_shutdown(void);

#ifdef __cplusplus
}
#endif

#endif

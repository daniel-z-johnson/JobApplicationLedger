### Coding models
- Llama-3.1-8B-Instruct-Heretic
- https://huggingface.co/meta-llama/Llama-3.1-8B-Instruct
- remember GGUF to create ollama files example below
```
# Content of your Modelfile
FROM ./Llama-3.1-8B-Instruct-heretic.Q4_K_M.gguf

# Optional: Set a system prompt to reinforce coding behavior
SYSTEM "You are an expert Java and Spring Boot engineer. Write clean, production-ready code."
```
- https://ollama.com/richardyoung/llama-3.2-3b-instruct-abliterated

### Research
- safetensors to gguf or ggml files

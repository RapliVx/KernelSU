A based-on [`Backslashxx/KernelSU`](https://github.com/backslashxx/KernelSU)

## Integration
```sh
curl -LSs "https://raw.githubusercontent.com/RapliVx/KernelSU/xxksu/kernel/setup.sh" | bash -s xxksu
```

## Instruction
- Remove all manual hook implementation
- Disable ``CONFIG_KPROBES``
- Enable ``CONFIG_KSU`` and ``CONFIG_KSU_TAMPER_SYSCALL_TABLE``
- If you want add avc log spoofing enable ``CONFIG_KSU_EXTRAS``

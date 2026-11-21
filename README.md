基于  Backslashxx/KernelSU  开发(https://github.com/backslashxx/KernelSU)

集成方式

```sh
curl -LSs "https://raw.githubusercontent.com/RapliVx/KernelSU/xxksu/kernel/setup.sh" | bash -s xxksu
```

说明
 
- 移除所有手动钩子（hook）实现代码
​
- 关闭  CONFIG_KPROBES 
​
- 开启  CONFIG_KSU  以及  CONFIG_KSU_TAMPER_SYSCALL_TABLE 
​
- 如需启用 AVC 日志伪装功能，请开启  CONFIG_KSU_EXTRAS 
 
 
 
术语注解（内核开发相关）
 
1. hook：钩子，内核劫持、挂钩函数
​
2. CONFIG_KPROBES：内核探针，内核动态插桩调试功能
​
3. syscall table：系统调用表
​
4. AVC log：SELinux 的访问向量缓存日志（SELinux 权限审计日志）
​
5. spoofing：伪装、欺骗（这里指隐藏 SELinux 相关日志，规避检测）
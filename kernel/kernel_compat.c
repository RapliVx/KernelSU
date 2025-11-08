#include <linux/version.h>
#include <linux/fs.h>
#include <linux/nsproxy.h>
#if LINUX_VERSION_CODE >= KERNEL_VERSION(4, 10, 0)
#include <linux/sched/task.h>
#else
#include <linux/sched.h>
#endif
#include <linux/uaccess.h>
#include <linux/filter.h>
#include <linux/seccomp.h>
#include "klog.h" // IWYU pragma: keep
#include "kernel_compat.h" // Add check Huawei Device
#if LINUX_VERSION_CODE < KERNEL_VERSION(4, 10, 0) || defined(CONFIG_IS_HW_HISI) || defined(CONFIG_KSU_ALLOWLIST_WORKAROUND)
#include <linux/key.h>
#include <linux/errno.h>
#include <linux/cred.h>
struct key *init_session_keyring = NULL;

static inline int install_session_keyring(struct key *keyring)
{
	struct cred *new;
	int ret;

	new = prepare_creds();
	if (!new)
		return -ENOMEM;

	ret = install_session_keyring_to_cred(new, keyring);
	if (ret < 0) {
		abort_creds(new);
		return ret;
	}

	return commit_creds(new);
}
#endif

struct action_cache {
	DECLARE_BITMAP(allow_native, SECCOMP_ARCH_NATIVE_NR);
#ifdef SECCOMP_ARCH_COMPAT
	DECLARE_BITMAP(allow_compat, SECCOMP_ARCH_COMPAT_NR);
#endif
};

struct seccomp_filter {
	refcount_t refs;
	refcount_t users;
	bool log;
#if LINUX_VERSION_CODE >= KERNEL_VERSION(6, 1, 0)
	bool wait_killable_recv;
#endif
	struct action_cache cache;
	struct seccomp_filter *prev;
	struct bpf_prog *prog;
	struct notification *notif;
	struct mutex notify_lock;
	wait_queue_head_t wqh;
};

void ksu_seccomp_clear_cache(struct seccomp_filter *filter, int nr)
{
    if (!filter) {
        return;
    }

    if (nr >= 0 && nr < SECCOMP_ARCH_NATIVE_NR) {
        clear_bit(nr, filter->cache.allow_native);
    }

#ifdef SECCOMP_ARCH_COMPAT
    if (nr >= 0 && nr < SECCOMP_ARCH_COMPAT_NR) {
        clear_bit(nr, filter->cache.allow_compat);
    }
#endif
}

void ksu_seccomp_allow_cache(struct seccomp_filter *filter, int nr)
{
    if (!filter) {
        return;
    }

    if (nr >= 0 && nr < SECCOMP_ARCH_NATIVE_NR) {
        set_bit(nr, filter->cache.allow_native);
    }

#ifdef SECCOMP_ARCH_COMPAT
    if (nr >= 0 && nr < SECCOMP_ARCH_COMPAT_NR) {
        set_bit(nr, filter->cache.allow_compat);
    }
#endif
}

static inline int ksu_access_ok(const void *addr, unsigned long size)
{
#if LINUX_VERSION_CODE >= KERNEL_VERSION(5,0,0)
	return access_ok(addr, size);
#else
	return access_ok(VERIFY_READ, addr, size);
#endif
}

long ksu_strncpy_from_user_retry(char *dst, const void __user *unsafe_addr,
				   long count)
{
	long ret = strncpy_from_user_nofault(dst, unsafe_addr, count);
	if (likely(ret >= 0))
		return ret;

	// we faulted! fallback to slow path
	if (unlikely(!ksu_access_ok(unsafe_addr, count)))
		return -EFAULT;

	return strncpy_from_user(dst, unsafe_addr, count);
}

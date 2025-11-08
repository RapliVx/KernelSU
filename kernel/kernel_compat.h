#ifndef __KSU_H_KERNEL_COMPAT
#define __KSU_H_KERNEL_COMPAT

#include <linux/fs.h>
#include <linux/version.h>
#include <linux/list.h>

/**
 * list_count_nodes - count the number of nodes in a list
 * the head of the list
 * 
 * Returns the number of nodes in the list
 */
#if LINUX_VERSION_CODE < KERNEL_VERSION(6, 6, 0)
static inline size_t list_count_nodes(const struct list_head *head)
{
	const struct list_head *pos;
	size_t count = 0;

	if (!head)
		return 0;

	list_for_each(pos, head) {
		count++;
	}
	
	return count;
}
#endif

extern long ksu_strncpy_from_user_retry(char *dst,
					  const void __user *unsafe_addr,
					  long count);

#if LINUX_VERSION_CODE < KERNEL_VERSION(4, 10, 0) || defined(CONFIG_IS_HW_HISI) || defined(CONFIG_KSU_ALLOWLIST_WORKAROUND)
extern struct key *init_session_keyring;
#endif

extern void ksu_seccomp_clear_cache(struct seccomp_filter *filter, int nr);
extern void ksu_seccomp_allow_cache(struct seccomp_filter *filter, int nr);

#endif

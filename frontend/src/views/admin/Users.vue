<template>
  <div class="users-page fade-in">
    <div class="page-header">
      <div>
        <p class="eyebrow">Admin · 用户</p>
        <h2>用户管理</h2>
      </div>
      <p class="page-stats text-sm text-muted">共 {{ users.length }} 个用户</p>
    </div>

    <!-- 用户列表 -->
    <div class="card table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>
              <div class="user-cell">
                <div class="user-avatar">{{ user.username?.charAt(0)?.toUpperCase() }}</div>
                <span>{{ user.username }}</span>
              </div>
            </td>
            <td>{{ user.email || '-' }}</td>
            <td>
              <span class="tag role-tag" :class="user.role?.toLowerCase()">
                {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>
              <span class="status status-tag" :class="{ active: user.status === 1, disabled: user.status === 0 }">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn--sm" :class="user.status === 1 ? 'btn--warning' : 'btn--success'" @click="toggleStatus(user)">
                  {{ user.status === 1 ? '禁用' : '启用' }}
                </button>
                <button class="btn btn--sm btn--danger" @click="deleteUser(user)" v-if="user.role !== 'ADMIN'">
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="users.length === 0" class="empty-state">
        <p>暂无用户数据</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const users = ref([])

onMounted(() => {
  loadUsers()
})

const loadUsers = async () => {
  try {
    const res = await api.get('/user/list')
    users.value = res.data || []
  } catch (e) { console.error(e) }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const toggleStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${user.username} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await api.put('/user/status', {
      id: user.id,
      status: newStatus
    })

    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？此操作不可恢复。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    await api.delete(`/user/${user.id}`)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.users-page {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--spacing-5);
}

.page-header .eyebrow {
  margin-bottom: 2px;
}

.page-header h2 {
  font-size: 24px;
  margin: 0;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--color-divider);
  font-size: 13px;
}

.data-table th {
  background: var(--color-surface-subtle);
  font-weight: 600;
  color: var(--color-text-body);
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.data-table tbody tr:hover {
  background: var(--color-surface-hover);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-accent-light);
  color: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.role-tag.admin {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.role-tag.user {
  background: var(--color-surface-subtle);
  color: var(--color-text-secondary);
}

.status-tag.active {
  background: var(--color-success-light);
  color: var(--color-success);
}

.status-tag.disabled {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

.action-btns {
  display: flex;
  gap: var(--spacing-2);
}

.btn--warning {
  background: #FEF3C7;
  border-color: transparent;
  color: #B45309;
}

.btn--warning:hover {
  background: #B45309;
  color: white;
}

.btn--success {
  background: var(--color-success-light);
  border-color: transparent;
  color: var(--color-success);
}

.btn--success:hover {
  background: var(--color-success);
  color: white;
}

.btn--danger {
  background: var(--color-danger-light);
  border-color: transparent;
  color: var(--color-danger);
}

.btn--danger:hover {
  background: var(--color-danger);
  color: white;
}
</style>

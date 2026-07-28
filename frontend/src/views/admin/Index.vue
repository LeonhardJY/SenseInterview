<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <h3>管理后台</h3>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          数据看板
        </router-link>
        <router-link to="/admin/users" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4-4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>
          用户管理
        </router-link>
        <router-link to="/admin/questions" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          题库管理
        </router-link>
        <router-link to="/admin/interviews" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6"/><path d="M16 13H8"/><path d="M16 17H8"/><path d="M10 9H8"/></svg>
          面试记录
        </router-link>
        <router-link to="/admin/jobs" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 002 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/></svg>
          岗位管理
        </router-link>
        <router-link to="/lobby" class="nav-item back">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回前台
        </router-link>
      </nav>
    </aside>
    <main class="admin-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: calc(100vh - 64px);
  gap: var(--space-5);
}

.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
}

.sidebar-header {
  padding: var(--space-4) 0;
  margin-bottom: var(--space-4);
}

.sidebar-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--gray-900);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  font-size: 14px;
  color: var(--gray-600);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: var(--transition);
  text-decoration: none;
}

.nav-item:hover {
  background: var(--gray-50);
  color: var(--gray-900);
}

.nav-item.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 500;
}

.nav-item.back {
  margin-top: var(--space-4);
  border-top: 1px solid var(--border-color);
  padding-top: var(--space-4);
  color: var(--gray-500);
}

.nav-item svg {
  width: 18px;
  height: 18px;
}

.admin-content {
  flex: 1;
  min-width: 0;
}
</style>

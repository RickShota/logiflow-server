<script setup lang="ts">
import { NButton, NCard, NSpace, NTag, useMessage } from 'naive-ui'

const message = useMessage()

function testNaiveUi() {
  message.success('Naive UI 已就绪')
}
</script>

<template>
  <main class="page">
    <n-card class="card" title="LogiFlow">
      <p class="desc">企业级物资与订单流转系统 · 前端脚手架</p>
      <n-space>
        <n-tag type="success">Vue 3</n-tag>
        <n-tag type="info">TypeScript</n-tag>
        <n-tag type="warning">Naive UI</n-tag>
        <n-tag>Pinia</n-tag>
        <n-tag>Vue Router</n-tag>
        <n-tag>Vite</n-tag>
      </n-space>
      <template #footer>
        <n-button type="primary" @click="testNaiveUi">测试 Naive UI</n-button>
      </template>
    </n-card>
  </main>
</template>

<style scoped>
.page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
}

.card {
  max-width: 640px;
}

.desc {
  margin-bottom: 1rem;
}
</style>

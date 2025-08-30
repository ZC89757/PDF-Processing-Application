<template>
  <div class="summary-panel">
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
    </div>
    <div v-else class="summary-content">
      <h2 class="summary-title">AI文档摘要</h2>
      <p>{{ summary }}</p>
    </div>
    <div v-if="!summary && !loading" class="no-summary">
      暂未生成AI摘要
    </div>
  </div>
</template>

<script>
export default {
  name: 'SummaryPanel',
  props: {
    summary: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.summary-panel {
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  transition: background-color 0.3s ease;
  overflow-y: auto;
  overflow-x: hidden; /* 禁止横向滚动 */
  scroll-behavior: smooth;
  white-space: normal; /* 确保文本自动换行 */
  word-wrap: break-word; /* 确保长单词自动换行 */
}

.summary-panel:hover {
  background-color: rgba(0, 0, 0, 0.9);
}

.loading {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #3B82F6; /* 蓝色 */
  animation: spin 1.5s linear infinite;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.5);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.summary-title {
  color: #4ADE80; /* 霓虹绿色 */
  font-family: 'Roboto', sans-serif;
  margin-top: 0; /* 移除顶部边距 */
  margin-bottom: 15px;
  text-shadow: 0 0 10px rgba(74, 222, 128, 0.5);
}

.summary-content {
  padding: 0; /* 移除内边距 */
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.summary-content p {
  line-height: 1.8;
  color: #E5E7EB;
  margin-bottom: 15px;
}

.no-summary {
  color: #9CA3AF;
  text-align: center;
  padding: 40px 20px;
}

/* 滚动条样式 */
.summary-panel::-webkit-scrollbar {
  width: 6px;
}

.summary-panel::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
}

.summary-panel::-webkit-scrollbar-thumb {
  background: #4ADE80;
  border-radius: 3px;
}

@import url('https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap');
</style>
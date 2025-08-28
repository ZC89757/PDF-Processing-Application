<template>
  <div class="summary-panel">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else class="summary-content">
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
}

.loading {
  padding: 20px;
}

.summary-content p {
  line-height: 1.6;
  color: #E5E7EB;
}

.no-summary {
  color: #9CA3AF;
  text-align: center;
  padding: 40px 20px;
}
</style>
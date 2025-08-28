<template>
  <div class="outline-tree">
    <ul class="outline-list">
      <li 
        v-for="item in outlines" 
        :key="item.id" 
        class="outline-item"
        :class="{ 'level-' + item.level: true }"
        @click="navigate(item.page)"
      >
        <span class="outline-title">{{ item.title }}</span>
        <span class="outline-page">p.{{ item.page }}</span>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'OutlineTree',
  props: {
    outlines: {
      type: Array,
      default: () => []
    }
  },
  emits: ['navigate'],
  methods: {
    navigate(page) {
      this.$emit('navigate', page)
    }
  }
}
</script>

<style scoped>
.outline-tree {
  color: #E5E7EB;
}

.outline-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.outline-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.outline-item:hover {
  background-color: #111827;
}

.outline-item.level-1 {
  padding-left: 12px;
}

.outline-item.level-2 {
  padding-left: 24px;
}

.outline-item.level-3 {
  padding-left: 36px;
}

.outline-item.level-4 {
  padding-left: 48px;
}

.outline-item.level-5 {
  padding-left: 60px;
}

.outline-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.outline-page {
  color: #9CA3AF;
  font-size: 12px;
  margin-left: 10px;
}
</style>
<template>
  <div class="outline-tree">
    <div
      v-for="item in outline"
      :key="item.id"
      class="outline-item"
      :class="{ ['level-' + item.level]: true }"
      @click="navigate(item.page)"
    >
      {{ item.title }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'OutlineTree',
  props: {
    outline: {
      type: Array,
      required: true
    }
  },
  methods: {
    navigate(page) {
      this.$emit('navigate', page);
    }
  }
}
</script>

<style scoped>
.outline-tree {
  padding: 10px;
}

.outline-item {
  cursor: pointer;
  padding: 5px 0;
  border-left: 2px solid transparent;
}

.outline-item:hover {
  background-color: #f0f0f0;
}

.level-1 {
  padding-left: 10px;
  font-size: 16px;
  font-weight: bold;
}

.level-2 {
  padding-left: 20px;
  font-size: 14px;
}

.level-3 {
  padding-left: 30px;
  font-size: 12px;
}

.level-4 {
  padding-left: 40px;
  font-size: 11px;
}

.level-5 {
  padding-left: 50px;
  font-size: 10px;
}
</style>
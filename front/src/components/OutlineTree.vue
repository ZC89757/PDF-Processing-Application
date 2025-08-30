<template>
  <div class="outline-tree">
    <div class="particles-container">
      <div v-for="i in 20" :key="i" class="particle"></div>
    </div>
    <div
      v-for="item in outlines"
      :key="item.id"
      class="outline-item"
      :class="{ ['level-' + item.level]: true, 'active': activePage === item.page }"
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
    outlines: {
      type: Array,
    }
  },
  data() {
    return {
      activePage: null
    }
  },
  methods: {
    navigate(page) {
      this.activePage = page;
      this.$emit('navigate', page);
    }
  },
  mounted() {
    // 初始化粒子动画
    const particles = document.querySelectorAll('.particle');
    particles.forEach(particle => {
      this.animateParticle(particle);
    });
  },
  methods: {
    navigate(page) {
      this.activePage = page;
      this.$emit('navigate', page);
    },
    animateParticle(particle) {
      // 随机设置粒子的初始位置、大小和动画时间
      const size = Math.random() * 4 + 1;
      const posX = Math.random() * 100;
      const posY = Math.random() * 100;
      const delay = Math.random() * 2;
      const duration = Math.random() * 4 + 3;

      particle.style.width = `${size}px`;
      particle.style.height = `${size}px`;
      particle.style.left = `${posX}%`;
      particle.style.top = `${posY}%`;
      particle.style.animationDelay = `${delay}s`;
      particle.style.animationDuration = `${duration}s`;
      
      // 动画结束后重新开始
      particle.addEventListener('animationend', () => {
        this.animateParticle(particle);
      });
    }
  }
}
</script>

<style scoped>
.outline-tree {
  padding: 10px;
  position: relative;
  overflow: hidden;
}

.outline-item {
  cursor: pointer;
  padding: 8px 10px;
  border-left: 2px solid transparent;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
  margin-bottom: 4px;
  border-radius: 4px;
}

.outline-item:hover {
  background-color: #4299E1; /* 霓虹蓝色 */
  color: white;
  box-shadow: 0 0 10px rgba(66, 153, 225, 0.6); /* 蓝色光效 */
  transform: translateX(5px);
}

.outline-item.active {
  background-color: #1A365D; /* 深蓝色 */
  color: #4ADE80; /* 亮绿色 */
  border-left: 2px solid #4ADE80;
}

/* 展开/折叠动画 */
.outline-item {
  max-height: 100px;
  opacity: 1;
  overflow: hidden;
  transition: max-height 0.3s ease, opacity 0.3s ease, background-color 0.3s ease, color 0.3s ease, transform 0.3s ease;
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

/* 粒子动画 */
.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.particle {
  position: absolute;
  background-color: rgba(147, 197, 253, 0.5); /* 浅蓝色 */
  border-radius: 50%;
  pointer-events: none;
  animation: float 5s linear infinite;
}

@keyframes float {
  0% {
    transform: translate(0, 0);
    opacity: 0;
  }
  25% {
    opacity: 0.5;
  }
  50% {
    transform: translate(20px, -30px);
    opacity: 0.8;
  }
  75% {
    opacity: 0.5;
  }
  100% {
    transform: translate(0, -60px);
    opacity: 0;
  }
}
</style>
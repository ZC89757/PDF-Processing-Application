<template>
  <div class="app-container">
    <div class="dynamic-background">
      <div class="gradient-bg" ref="gradientBg"></div>
      <div class="particles-container" ref="particlesContainer"></div>
    </div>
    
    <div class="nav-bar" v-if="$route.path !== '/reader/' + $route.params.fileId">
      <div class="nav-logo">PDF智能处理系统</div>
      <div class="nav-links">
        <el-button type="primary" @click="showUploadDialog" class="upload-btn nav-upload-btn">
          <el-icon><Upload /></el-icon>
          上传 PDF
        </el-button>
      </div>
    </div>
    
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传 PDF 文件"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        drag
        action="/api/files/upload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 PDF 文件，大小不超过 200MB
          </div>
        </template>
      </el-upload>
    </el-dialog>
    
    <div class="content-container">
      <router-view ref="routerView" />
    </div>
  </div>
</template>

<script>
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  data() {
    return {
      gradientDirection: 'to right bottom',
      particles: [],
      animationFrameId: null,
      uploadDialogVisible: false
    }
  },
  mounted() {
    // 初始化动态渐变背景
    this.initDynamicBackground();
    
    // 初始化粒子动画
    this.initParticles();
    this.animateParticles();
  },
  beforeUnmount() {
    // 清除动画帧
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
    
    // 清除渐变方向变化的定时器
    if (this.gradientTimer) {
      clearInterval(this.gradientTimer);
    }
  },
  methods: {
    showUploadDialog() {
      this.uploadDialogVisible = true;
    },
    
    beforeUpload(file) {
      const isPDF = file.type === 'application/pdf';
      const isLt200M = file.size / 1024 / 1024 < 200;
      
      if (!isPDF) {
        this.$message.error('只能上传 PDF 文件!');
      }
      if (!isLt200M) {
        this.$message.error('文件大小不能超过 200MB!');
      }
      
      return isPDF && isLt200M;
    },
    
    handleUploadSuccess(response) {
      this.uploadDialogVisible = false;
      this.$message.success('上传成功!');
      this.$router.push('/');
      // 刷新文件列表
      if (this.$route.path === '/') {
        // 使用事件总线或直接调用子组件方法来刷新文件列表
        this.$nextTick(() => {
          const homeComponent = this.$refs.routerView?.$refs.home;
          if (homeComponent && typeof homeComponent.loadFiles === 'function') {
            homeComponent.loadFiles();
          } else {
            // 如果无法直接调用组件方法，则重新加载当前路由
            this.$router.go(0);
          }
        });
      }
    },
    
    handleUploadError() {
      this.$message.error('上传失败，请重试!');
    },
    
    initDynamicBackground() {
      // 设置初始渐变方向
      this.updateGradientDirection();
      
      // 每10秒改变一次渐变方向
      this.gradientTimer = setInterval(() => {
        this.updateGradientDirection();
      }, 10000);
    },
    
    updateGradientDirection() {
      const directions = [
        'to right bottom',
        'to left bottom',
        'to right top',
        'to left top',
        'to bottom',
        'to right',
        'to top',
        'to left'
      ];
      
      // 随机选择一个新的渐变方向
      let newDirection;
      do {
        newDirection = directions[Math.floor(Math.random() * directions.length)];
      } while (newDirection === this.gradientDirection);
      
      this.gradientDirection = newDirection;
      
      // 更新CSS变量
      document.documentElement.style.setProperty('--gradient-direction', this.gradientDirection);
    },
    
    initParticles() {
      const container = this.$refs.particlesContainer;
      const containerRect = container.getBoundingClientRect();
      
      // 创建粒子
      for (let i = 0; i < 50; i++) {
        const particle = document.createElement('div');
        particle.className = 'particle';
        
        // 随机位置
        const x = Math.random() * containerRect.width;
        const y = Math.random() * containerRect.height;
        
        // 随机大小
        const size = Math.random() * 5 + 2;
        
        // 随机速度和方向
        const vx = (Math.random() - 0.5) * 1;
        const vy = (Math.random() - 0.5) * 1;
        
        // 随机透明度
        const opacity = Math.random() * 0.5 + 0.3;
        
        // 设置样式
        particle.style.width = `${size}px`;
        particle.style.height = `${size}px`;
        particle.style.left = `${x}px`;
        particle.style.top = `${y}px`;
        particle.style.opacity = opacity;
        
        // 存储粒子属性
        this.particles.push({
          element: particle,
          x,
          y,
          vx,
          vy,
          size
        });
        
        // 添加到容器
        container.appendChild(particle);
      }
    },
    
    animateParticles() {
      const container = this.$refs.particlesContainer;
      const containerRect = container.getBoundingClientRect();
      
      // 更新每个粒子的位置
      this.particles.forEach(particle => {
        // 更新位置
        particle.x += particle.vx;
        particle.y += particle.vy;
        
        // 边界检查
        if (particle.x < 0) {
          particle.x = containerRect.width;
        } else if (particle.x > containerRect.width) {
          particle.x = 0;
        }
        
        if (particle.y < 0) {
          particle.y = containerRect.height;
        } else if (particle.y > containerRect.height) {
          particle.y = 0;
        }
        
        // 更新DOM元素位置
        particle.element.style.left = `${particle.x}px`;
        particle.element.style.top = `${particle.y}px`;
      });
      
      // 继续动画循环
      this.animationFrameId = requestAnimationFrame(this.animateParticles);
    }
  }
}
</script>

<style>
:root {
  --gradient-direction: to right bottom;
}

body {
  margin: 0;
  padding: 0;
  color: #fff;
  font-family: Arial, sans-serif;
  overflow-x: hidden;
}

.app-container {
  min-height: 100vh;
  position: relative;
}

/* 动态渐变背景 */
.dynamic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -2;
  overflow: hidden;
}

.gradient-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(var(--gradient-direction), #6a11cb, #2575fc);
  transition: background 2s ease;
  z-index: -2;
}

/* 粒子容器 */
.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

/* 粒子样式 */
.particle {
  position: absolute;
  background-color: rgba(100, 200, 255, 0.7);
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(100, 200, 255, 0.5);
}

/* 导航栏 */
.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background-color: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.nav-logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 10px rgba(100, 200, 255, 0.7);
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-link {
  color: #fff;
  text-decoration: none;
  padding: 8px 15px;
  border-radius: 4px;
  background-color: rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

.nav-link:hover {
  background-color: rgba(59, 130, 246, 0.8);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.5);
  transform: scale(1.1);
  width: 110%;
}

.nav-upload-btn {
  background: linear-gradient(135deg, #4CAF50, #2196F3);
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: bold;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.nav-upload-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 0 20px rgba(33, 150, 243, 0.6);
  background: linear-gradient(135deg, #2E7D32, #1565C0);
}

.nav-upload-btn:active {
  transform: scale(0.95);
}

/* 内容容器 */
.content-container {
  padding: 20px;
  position: relative;
  z-index: 1;
}

/* 卡片通用样式 */
.el-card {
  background-color: rgba(30, 41, 59, 0.7) !important;
  border: none !important;
  border-radius: 10px !important;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2), 
              0 5px 10px rgba(0, 0, 0, 0.1),
              0 0 0 1px rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease !important;
  transform-style: preserve-3d;
  perspective: 1000px;
}

.el-card:hover {
  transform: scale(1.05) translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.3),
              0 10px 15px rgba(0, 0, 0, 0.2),
              0 0 0 1px rgba(255, 255, 255, 0.2) !important;
}
</style>
<template>
  <div class="reader">
    <div class="reader-layout">
      <!-- 左侧目录 -->
      <div class="outline-panel">
        <h3>文档目录</h3>
        <outline-tree v-if="outlines" :outlines="outlines" @navigate="goToPage" />
      </div>
      
      <!-- 中间PDF查看器 -->
      <div class="pdf-viewer-panel">
        <div class="pdf-toolbar">
          <el-button class="nav-button prev-button" @click="prevPage" :disabled="currentPage <= 1">上一页</el-button>
          <span class="page-indicator">第 {{ currentPage }} 页 / 共 {{ pageCount }} 页</span>
          <el-button class="nav-button next-button" @click="nextPage" :disabled="currentPage >= pageCount">下一页</el-button>
          <div class="zoom-controls">
            <el-button class="zoom-button" @click="zoomIn">+</el-button>
            <el-button class="zoom-button" @click="zoomOut">-</el-button>
            <el-button class="zoom-button" @click="resetZoom">重置</el-button>
          </div>
        </div>
        <div class="pdf-container" ref="pdfContainer"></div>
      </div>
      
      <!-- 右侧摘要 -->
      <div class="summary-panel">
        <summary-panel :summary="summary" :loading="summaryLoading" />
      </div>
    </div>
  </div>
</template>

<script>
import OutlineTree from '../components/OutlineTree.vue'
import SummaryPanel from '../components/SummaryPanel.vue'
import axios from 'axios'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/build/pdf'
import pdfjsWorker from 'pdfjs-dist/build/pdf.worker?url'

GlobalWorkerOptions.workerSrc = pdfjsWorker

export default {
  name: 'Reader',
  components: {
    OutlineTree,
    SummaryPanel
  },
  props: {
    fileId: {
      type: String,
      required: true
    },
    page: {
      type: Number,
      default: 1
    }
  },
  data() {
    return {
      outlines: [],
      summary: '',
      summaryLoading: false,
      currentPage: 1,
      pageCount: 0,
      scale: 1.0
    }
  },
  created() {
    this.pdfDoc= null
  },
  mounted() {
    this.loadOutline()
    this.loadSummary()
    this.loadPdf()
  },
  watch: {
    page(newPage) {
      this.currentPage = newPage
      this.renderPage()
    }
  },
  methods: {
    async loadOutline() {
      try {
        const response = await axios.get(`/api/files/${this.fileId}/outline`)
        this.outlines = response.data.data || []
      } catch (error) {
        console.error('加载目录失败:', error)
      }
    },
    async loadSummary() {
      this.summaryLoading = true
      try {
        const response = await axios.get(`/api/files/${this.fileId}/summary`)
        this.summary = response.data.data || '暂未生成AI摘要'
      } catch (error) {
        this.summary = '获取摘要失败'
      } finally {
        this.summaryLoading = false
      }
    },
    async loadPdf() {
      try {
        const response = await axios.get(`/api/files/${this.fileId}/pdf`, { responseType: 'arraybuffer' })
        this.pdfDoc = await getDocument({ data: response.data }).promise
        this.pageCount = this.pdfDoc.numPages
        this.currentPage = this.page ?? 1
        await this.renderPage()
      } catch (error) {
        console.error('加载PDF失败:', error)
      }
    },
    async renderPage() {
      if (!this.pdfDoc) return
      try {
        const page = await this.pdfDoc.getPage(this.currentPage)
        const viewport = page.getViewport({ scale: this.scale })
        
        // 清空容器
        const container = this.$refs.pdfContainer
        container.innerHTML = ''
        
        // 创建canvas
        const canvas = document.createElement('canvas')
        const context = canvas.getContext('2d')
        canvas.height = viewport.height
        canvas.width = viewport.width
        canvas.className = 'pdf-canvas'
        
        // 添加翻页动画类
        canvas.classList.add('page-transition')
        
        // 渲染页面
        const renderContext = {
          canvasContext: context,
          viewport: viewport
        }
        await page.render(renderContext).promise
        
        container.appendChild(canvas)
        
        // 触发动画
        setTimeout(() => {
          canvas.classList.add('page-visible')
        }, 10)
      } catch (error) {
        console.error('渲染页面失败:', error)
      }
    },
    prevPage() {
      if (this.currentPage <= 1) return
      this.currentPage--
      this.updateRoute()
    },
    nextPage() {
      if (this.currentPage >= this.pageCount) return
      this.currentPage++
      this.updateRoute()
    },
    goToPage(page) {
      this.currentPage = page
      this.updateRoute()
    },
    updateRoute() {
      this.$router.replace({ 
        name: 'reader', 
        params: { fileId: this.fileId },
        query: { page: this.currentPage }
      })
    },
    zoomIn() {
      this.scale += 0.1
      this.renderPage()
    },
    zoomOut() {
      if (this.scale > 0.5) {
        this.scale -= 0.1
        this.renderPage()
      }
    },
    resetZoom() {
      this.scale = 1.0
      this.renderPage()
    }
  }
}
</script>

<style scoped>
.reader {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.reader-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.outline-panel {
  width: 250px;
  background-color: rgba(31, 41, 55, 0.85); /* 增加透明度 */
  padding: 20px;
  overflow-y: auto;
  border-right: 1px solid #374151;
  height: 100vh; /* 确保高度占满可视区域 */
  max-height: 100vh; /* 限制最大高度 */
  box-sizing: border-box; /* 确保padding不会增加元素总高度 */
}

.pdf-viewer-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100vh; /* 确保高度占满可视区域 */
  max-height: 100vh; /* 限制最大高度 */
}

.pdf-toolbar {
  padding: 15px;
  background-color: rgba(17, 24, 39, 0.85); /* 增加透明度 */
  display: flex;
  align-items: center;
  gap: 15px;
  border-bottom: 1px solid #374151;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.nav-button {
  transition: all 0.3s ease;
  background-color: #374151;
  border-color: #4B5563;
  position: relative;
  overflow: hidden;
}

.nav-button:not(:disabled):hover {
  transform: scale(1.1);
  background-color: #3B82F6;
  border-color: #3B82F6;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.5);
}

.nav-button:not(:disabled):active {
  transform: scale(0.95);
}

/* 禁用状态的按钮样式 */
.nav-button:disabled {
  background-color: rgba(55, 65, 81, 0.5);
  border-color: rgba(75, 85, 99, 0.5);
  color: rgba(229, 231, 235, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
}

.zoom-controls {
  display: flex;
  gap: 5px;
  margin-left: auto;
}

.zoom-button {
  transition: all 0.3s ease;
  background-color: #374151;
  border-color: #4B5563;
}

.zoom-button:not(:disabled):hover {
  transform: scale(1.1);
  background-color: #3B82F6;
  border-color: #3B82F6;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.5);
}

.zoom-button:not(:disabled):active {
  transform: scale(0.95);
}

/* 禁用状态的缩放按钮样式 */
.zoom-button:disabled {
  background-color: rgba(55, 65, 81, 0.5);
  border-color: rgba(75, 85, 99, 0.5);
  color: rgba(229, 231, 235, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
}

.page-indicator {
  font-weight: bold;
  color: #E5E7EB;
}

.pdf-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 30px;
  background-color: #000000;
  box-shadow: inset 0 0 50px rgba(59, 130, 246, 0.2);
}

.pdf-canvas {
  box-shadow: 0 0 30px rgba(59, 130, 246, 0.3);
  border-radius: 5px;
}

/* 翻页动画 */
.page-transition {
  opacity: 0;
  transform: scale(0.9);
  transition: all 0.3s ease-out;
}

.page-visible {
  opacity: 1;
  transform: scale(1);
}

.summary-panel {
  width: 300px;
  background-color: rgba(31, 41, 55, 0.85); /* 增加透明度 */
  padding: 20px;
  overflow-y: auto;
  border-left: 1px solid #374151;
  height: 100vh; /* 确保高度占满可视区域 */
  max-height: 100vh; /* 限制最大高度 */
  box-sizing: border-box; /* 确保padding不会增加元素总高度 */
}

.outline-panel h3,
.summary-panel h3 {
  margin-top: 0;
  color: #E5E7EB;
}
</style>
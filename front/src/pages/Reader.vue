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
          <el-button @click="prevPage" :disabled="currentPage <= 1">上一页</el-button>
          <span>第 {{ currentPage }} 页 / 共 {{ pageCount }} 页</span>
          <el-button @click="nextPage" :disabled="currentPage >= pageCount">下一页</el-button>
          <el-button @click="zoomIn">+</el-button>
          <el-button @click="zoomOut">-</el-button>
          <el-button @click="resetZoom">重置</el-button>
        </div>
        <div class="pdf-container" ref="pdfContainer"></div>
      </div>
      
      <!-- 右侧摘要 -->
      <div class="summary-panel">
        <h3>AI 摘要</h3>
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
        
        // 渲染页面
        const renderContext = {
          canvasContext: context,
          viewport: viewport
        }
        await page.render(renderContext).promise
        
        container.appendChild(canvas)
      } catch (error) {
        console.error('渲染页面失败:', error)
      }
    },
    prevPage() {
      if (this.currentPage <= 1) return
      this.currentPage--
      this.updateRoute()
      this.renderPage()
    },
    nextPage() {
      if (this.currentPage >= this.pageCount) return
      this.currentPage++
      this.updateRoute()
      this.renderPage()
    },
    goToPage(page) {
      this.currentPage = page
      this.updateRoute()
      this.renderPage()
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
  background-color: #1F2937;
  padding: 20px;
  overflow-y: auto;
  border-right: 1px solid #374151;
}

.pdf-viewer-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.pdf-toolbar {
  padding: 10px;
  background-color: #1F2937;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #374151;
}

.pdf-container {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  background-color: #0F172A;
}

.summary-panel {
  width: 300px;
  background-color: #1F2937;
  padding: 20px;
  overflow-y: auto;
  border-left: 1px solid #374151;
}

.outline-panel h3,
.summary-panel h3 {
  margin-top: 0;
  color: #E5E7EB;
}
</style>
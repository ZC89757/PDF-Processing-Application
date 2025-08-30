<template>
  <div class="file-card" @click="$emit('click', {id: file.id, page: file.page})">
    <div class="card-content">
      <div class="thumbnail">
        <!-- PDF 第一页缩略图 -->
        <canvas 
          v-if="file.status === 'READY'" 
          ref="thumbnailCanvas" 
          class="thumbnail-canvas"
          width="80"
          height="100"
        ></canvas>
        <div v-else class="thumbnail-placeholder">PDF</div>
      </div>
      <div class="file-info">
        <h3 class="filename">{{ file.filename }}</h3>
        <p class="upload-time">{{ formatTime(file.upload_time) }}</p>
        <div class="status">
          <el-tag :type="getStatusType(file.status)" size="small">
            {{ getStatusText(file.status) }}
          </el-tag>
          <el-tag v-if="file.has_outline" type="success" size="small" style="margin-left: 5px;">
            有目录
          </el-tag>
          <el-tag v-if="file.page" type="info" size="small" style="margin-left: 5px;">
            第 {{ file.page }} 页
          </el-tag>
        </div>
        <div class="actions">
          <el-button 
            size="small" 
            :type="file.summary_exists ? 'success' : 'warning'"
            @click.stop="generateSummary"
          >
            {{ file.summary_exists ? '查看摘要' : '生成摘要' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
// 引入PDF.js
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist/build/pdf'
import pdfjsWorker from 'pdfjs-dist/build/pdf.worker?url'

GlobalWorkerOptions.workerSrc = pdfjsWorker

export default {
  name: 'FileCard',
  props: {
    file: {
      type: Object,
      required: true
    }
  },
  emits: ['click'],
  mounted() {
    // 组件挂载后生成缩略图
    if (this.file.status === 'READY') {
      this.generateThumbnail()
    }
  },
  methods: {
    formatTime(time) {
      return new Date(time).toLocaleString('zh-CN')
    },
    getStatusText(status) {
      const statusMap = {
        'UPLOADED': '已上传',
        'UPLOADING': '上传中',
        'PARSING': '解析中',
        'READY': '已完成',
        'FAILED': '失败'
      }
      return statusMap[status] || status
    },
    getStatusType(status) {
      const typeMap = {
        'UPLOADED': '',
        'UPLOADING': '',
        'PARSING': 'warning',
        'READY': 'success',
        'FAILED': 'danger'
      }
      return typeMap[status] || ''
    },
    generateSummary() {
      axios.get(`/api/files/${this.file.id}/summary`)
        .then(response => {
          this.$message.success('摘要: ' + response.data.data)
        })
        .catch(error => {
          this.$message.error('获取摘要失败: ' + error.message)
        })
    },
    // 生成PDF缩略图
    async generateThumbnail() {
      try {
        // 获取PDF文件
        const response = await axios({
          method: 'get',
          url: `/api/files/${this.file.id}/pdf`,
          responseType: 'arraybuffer' // 确保获取二进制数据
        });

        const pdf = await getDocument({ data: response.data }).promise;

        // 加载PDF文档的第一页
        const page = await pdf.getPage(1);

        // 设置缩略图尺寸
        const scale = 0.2; // 缩放比例
        const viewport = page.getViewport({ scale: scale });

        // 获取canvas并设置尺寸
        const canvas = this.$refs.thumbnailCanvas;
        const context = canvas.getContext('2d');

        if (context) {
          // 设置canvas的宽高
          canvas.width = viewport.width;
          canvas.height = viewport.height;

          // 渲染第一页到canvas
          await page.render({
            canvasContext: context,
            viewport: viewport
          }).promise;
        } else {
          console.error('无法获取canvas上下文');
        }
      } catch (error) {
        console.error('生成PDF缩略图失败:', error);
      }
    }
  }
}
</script>

<style scoped>
.file-card {
  background-color: #1F2937;
  border-radius: 12px;
  padding: 15px;
  cursor: pointer;
  transition: transform 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.file-card:hover {
  transform: scale(1.01);
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.card-content {
  display: flex;
}

.thumbnail {
  width: 80px;
  height: 100px;
  margin-right: 15px;
}

.thumbnail-placeholder {
  width: 100%;
  height: 100%;
  background-color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
  font-weight: bold;
  border-radius: 4px;
}

.thumbnail-canvas {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.file-info {
  flex: 1;
}

.filename {
  margin: 0 0 10px 0;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.upload-time {
  margin: 5px 0;
  font-size: 14px;
  color: #9CA3AF;
}

.status {
  margin: 10px 0;
}

.actions {
  margin-top: 10px;
}
</style>
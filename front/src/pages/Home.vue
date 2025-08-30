<template>
  <div class="home">
    <header class="header">
      <h1>PDF 文档阅读与分析系统</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showUploadDialog" class="upload-btn">
          <el-icon><Upload /></el-icon>
          上传 PDF
        </el-button>
      </div>
    </header>
    
    <div class="search-container">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索内容..."
        class="search-input"
        @keyup.enter="searchFiles"
      >
        <template #append>
          <el-button @click="searchFiles">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>
    
    <div class="file-list">
      <file-card
        v-for="file in fileList"
        :key="file.id"
        :file="file"
        @click="goToReader"
      />
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
  </div>
</template>

<script>
import FileCard from '../components/FileCard.vue'
import axios from 'axios'

export default {
  name: 'Home',
  components: {
    FileCard
  },
  data() {
    return {
      fileList: [],
      uploadDialogVisible: false,
      searchKeyword: '',
      currentPage: 1,
      pageSize: 20
    }
  },
  mounted() {
    this.loadFiles()
  },
  methods: {
    showUploadDialog() {
      this.uploadDialogVisible = true
    },
    beforeUpload(file) {
      const isPDF = file.type === 'application/pdf'
      const isLt200M = file.size / 1024 / 1024 < 200
      
      if (!isPDF) {
        this.$message.error('只能上传 PDF 文件!')
      }
      if (!isLt200M) {
        this.$message.error('文件大小不能超过 200MB!')
      }
      
      return isPDF && isLt200M
    },
    handleUploadSuccess(response, file, fileList) {
      this.uploadDialogVisible = false
      this.$message.success('文件上传成功!')
      // 触发解析
      if (response.data && response.data.id) {
        axios.post(`/api/files/${response.data.id}/parse`)
      }
      this.loadFiles()
    },
    handleUploadError(error, file, fileList) {
      this.$message.error('文件上传失败: ' + error.message)
    },
    loadFiles() {
      axios.get('/api/files', {
        params: {
          page: this.currentPage,
          size: this.pageSize,
          keyword: this.searchKeyword
        }
      }).then(response => {
        this.fileList = response.data.data || []
      }).catch(error => {
        this.$message.error('加载文件列表失败: ' + error.message)
      })
    },
    searchFiles() {
      this.currentPage = 1
      this.loadFiles()
    },
    goToReader(fileInfo) {
      const fileId = fileInfo.id;
      const page = fileInfo.page || 1;
      
      this.$router.push({ 
        name: 'reader', 
        params: { fileId },
        query: { page }
      });
    }
  }
}
</script>

<style scoped>
.home {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h1 {
  margin: 0;
}

.upload-btn {
  background-color: #10B981;
  border-color: #10B981;
}

.search-container {
  max-width: 600px;
  margin: 0 auto 30px;
}

.search-input {
  border-radius: 12px;
  background-color: #111827;
}

.file-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.upload-demo {
  text-align: center;
}
</style>
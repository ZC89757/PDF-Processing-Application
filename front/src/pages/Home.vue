<template>
  <div class="home" ref="home">
    <div class="search-container" style="margin-top: -10px;">
      <el-input
        v-model="searchKeyword"
        :placeholder="currentPlaceholder"
        class="search-input"
        @keyup.enter="searchFiles"
      >
        <template #append>
          <el-button @click="searchFiles" class="search-button">
            <el-icon><Search /></el-icon>
            搜索
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
      searchKeyword: '',
      currentPage: 1,
      pageSize: 20,
      searchPlaceholders: [
        '输入关键词，探索知识的海洋...',
        '想找什么？PDF宝库任你搜索~',
        '关键词输入，智能搜索启动！'
      ],
      currentPlaceholder: ''
    }
  },
  mounted() {
    this.loadFiles()
    this.setRandomPlaceholder()
  },
  methods: {
    setRandomPlaceholder() {
      // 随机选择一个搜索提示词
      const randomIndex = Math.floor(Math.random() * this.searchPlaceholders.length);
      this.currentPlaceholder = this.searchPlaceholders[randomIndex];
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
  margin-bottom: 10px;
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
  max-width: 600px;
  margin: 0 auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.search-input .el-input__wrapper {
  background-color: rgba(255, 255, 255, 0.1) !important;
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

.search-input .el-input__inner {
  color: #fff !important;
  font-size: 16px;
}

.search-input .el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.6) !important;
}

.search-input .el-input-group__append {
  background: linear-gradient(135deg, #3B82F6, #1E40AF) !important;
  border: none !important;
  color: white !important;
  transition: all 0.3s ease;
  padding: 0 15px !important;
}

.search-input .el-input-group__append:hover {
  background: linear-gradient(135deg, #2563EB, #1E3A8A) !important;
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.5);
}

.search-button {
  background: transparent !important;
  border: none !important;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.search-button .el-icon {
  margin-right: 4px;
  font-size: 16px;
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
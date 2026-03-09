<template>
  <el-container direction="vertical">
    <!-- ====== DID 生成 ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Key /></el-icon>
        <span>DID 生成</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-button class="default-button" @click="handleGenerate" :loading="generateLoading">
        生成 DID
      </el-button>
      <div v-if="generateResult" class="result-block">
        <el-descriptions title="生成结果" :column="1" border>
          <el-descriptions-item label="DID">{{ generateResult.did }}</el-descriptions-item>
          <el-descriptions-item label="公钥">
            <el-tooltip :content="generateResult.publicKeyHex" placement="top">
              <span>{{ ellipsis(generateResult.publicKeyHex, 40) }}</span>
            </el-tooltip>
          </el-descriptions-item>
          <el-descriptions-item label="私钥">
            <el-tooltip :content="generateResult.privateKey" placement="top">
              <span>{{ ellipsis(generateResult.privateKey, 40) }}</span>
            </el-tooltip>
          </el-descriptions-item>
        </el-descriptions>
        <el-button class="default-button" style="margin-top: 10px;" @click="handleRegister" :loading="registerLoading">
          注册上链
        </el-button>
      </div>
    </div>

    <el-divider />

    <!-- ====== DID 查询 ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Search /></el-icon>
        <span>DID 查询</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form :inline="true" @submit.prevent="handleQuery">
        <el-form-item label="DID">
          <el-input v-model="queryDid" placeholder="请输入 DID，例如 did:mychain:xxxx" style="width: 420px;" />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleQuery" :loading="queryLoading">查询</el-button>
        </el-form-item>
      </el-form>
      <div v-if="queryResult" class="result-block">
        <el-descriptions title="查询结果" :column="1" border>
          <el-descriptions-item label="DID">{{ queryResult.id || queryResult.did }}</el-descriptions-item>
          <el-descriptions-item label="Controller">{{ queryResult.controller }}</el-descriptions-item>
          <el-descriptions-item label="Created">{{ queryResult.created }}</el-descriptions-item>
        </el-descriptions>
        <el-button link type="primary" style="margin-top:6px;" @click="showQueryRaw = !showQueryRaw">
          {{ showQueryRaw ? '收起原始数据' : '查看原始数据' }}
        </el-button>
        <pre v-if="showQueryRaw" class="json-block">{{ JSON.stringify(queryResult, null, 2) }}</pre>
      </div>
    </div>

    <el-divider />

    <!-- ====== DID 列表 ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><List /></el-icon>
        <span>DID 列表（本地库）</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-button class="default-button" style="margin-bottom:10px;" @click="handleListDids" :loading="listLoading">
        刷新列表
      </el-button>
      <el-table :data="didList" stripe style="width: 100%" max-height="400">
        <el-table-column label="DID" prop="did" min-width="320" align="center" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="120" align="center" />
        <el-table-column label="创建时间" prop="createdAt" width="200" align="center" show-overflow-tooltip />
        <el-table-column label="更新时间" prop="updatedAt" width="200" align="center" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="120" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleQueryFromList(scope.row.did)">
              链上查询
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ====== 生成结果详情 Dialog ====== -->
    <el-dialog v-model="generateDialogVisible" title="DID 生成详情" width="60%">
      <pre class="json-block">{{ JSON.stringify(generateRaw, null, 2) }}</pre>
      <template #footer>
        <el-button @click="generateDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { didGenerate, didRegister, didQuery, listDids } from '../../api/goSdk.js'

// ---- DID 生成 ----
const generateLoading = ref(false)
const registerLoading = ref(false)
const generateResult = ref<any>(null)
const generateRaw = ref<any>(null)
const generateDialogVisible = ref(false)
const lastDidDocument = ref<any>(null)

const handleGenerate = async () => {
  generateLoading.value = true
  generateResult.value = null
  try {
    const res = await didGenerate()
    const resp = res.data
    if (resp.code === 0) {
      const d = resp.data
      generateResult.value = {
        did: d.did || d.didDocument?.id,
        publicKeyHex: d.publicKeyHex || d.didDocument?.verificationMethod?.[0]?.publicKeyHex,
        privateKey: d.privateKey || d.privateKeyHex,
      }
      lastDidDocument.value = d.didDocument || d
      generateRaw.value = d
      ElMessage.success('DID 生成成功')
    } else {
      ElMessage.error(resp.message || '生成失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    generateLoading.value = false
  }
}

const handleRegister = async () => {
  if (!lastDidDocument.value) {
    ElMessage.warning('请先生成 DID')
    return
  }
  registerLoading.value = true
  try {
    const res = await didRegister(lastDidDocument.value)
    const resp = res.data
    if (resp.code === 0) {
      ElMessage.success('DID 注册上链成功')
      handleListDids() // 刷新列表
    } else {
      ElMessage.error(resp.message || '注册失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    registerLoading.value = false
  }
}

// ---- DID 查询 ----
const queryDid = ref('')
const queryLoading = ref(false)
const queryResult = ref<any>(null)
const showQueryRaw = ref(false)

const handleQuery = async () => {
  if (!queryDid.value.trim()) {
    ElMessage.warning('请输入 DID')
    return
  }
  queryLoading.value = true
  queryResult.value = null
  showQueryRaw.value = false
  try {
    const res = await didQuery(queryDid.value.trim())
    const resp = res.data
    if (resp.code === 0) {
      queryResult.value = resp.data
      ElMessage.success('查询成功')
    } else {
      ElMessage.error(resp.message || '查询失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    queryLoading.value = false
  }
}

const handleQueryFromList = (did: string) => {
  queryDid.value = did
  handleQuery()
}

// ---- DID 列表 ----
const didList = ref<any[]>([])
const listLoading = ref(false)

const handleListDids = async () => {
  listLoading.value = true
  try {
    const res = await listDids()
    const resp = res.data
    if (resp.code === 0) {
      didList.value = resp.data?.items || []
    } else {
      ElMessage.error(resp.message || '获取列表失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    listLoading.value = false
  }
}

// ---- 工具函数 ----
const ellipsis = (str: string, max: number) => {
  if (!str) return ''
  return str.length > max ? str.slice(0, max) + '...' : str
}

onMounted(() => {
  handleListDids()
})
</script>

<style scoped>
.section-body {
  padding: 10px 20px;
}
.result-block {
  margin-top: 12px;
}
.json-block {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  max-height: 400px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>

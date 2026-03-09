<template>
  <el-container direction="vertical">
    <!-- ====== 颁发 VC ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Stamp /></el-icon>
        <span>获取 VC（颁发凭证）</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form :model="issueForm" label-width="160px" style="max-width: 700px;">
        <el-form-item label="颁发者 DID">
          <el-input v-model="issueForm.issuerDid" placeholder="did:mychain:issuer" />
        </el-form-item>
        <el-form-item label="颁发者私钥">
          <el-input v-model="issueForm.issuerPrivateKey" placeholder="0x..." />
        </el-form-item>
        <el-form-item label="颁发者公钥">
          <el-input v-model="issueForm.issuerPublicKeyHex" placeholder="0x04..." />
        </el-form-item>
        <el-form-item label="持有者 DID">
          <el-input v-model="issueForm.holderDid" placeholder="did:mychain:holder" />
        </el-form-item>
        <el-form-item label="凭证类型">
          <el-input v-model="issueForm.credentialType" placeholder="例如 UniversityDegree" />
        </el-form-item>
        <el-form-item label="凭证内容 (JSON)">
          <el-input
            v-model="issueForm.credentialSubjectStr"
            type="textarea"
            :rows="4"
            placeholder='{"name":"alice","degree":"master","age":20}'
          />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleIssue" :loading="issueLoading">颁发 VC</el-button>
        </el-form-item>
      </el-form>
      <div v-if="issueResult" class="result-block">
        <el-alert title="VC 颁发成功" type="success" :closable="false" style="margin-bottom:8px;" />
        <el-button link type="primary" @click="showIssueRaw = !showIssueRaw">
          {{ showIssueRaw ? '收起原始数据' : '查看原始数据' }}
        </el-button>
        <pre v-if="showIssueRaw" class="json-block">{{ JSON.stringify(issueResult, null, 2) }}</pre>
      </div>
    </div>

    <el-divider />

    <!-- ====== 验证 VC ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><CircleCheck /></el-icon>
        <span>验证 VC</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form label-width="120px" style="max-width: 700px;">
        <el-form-item label="VC JSON">
          <el-input
            v-model="verifyVcStr"
            type="textarea"
            :rows="8"
            placeholder="请粘贴完整的 VC JSON 对象"
          />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleVerify" :loading="verifyLoading">验证 VC</el-button>
        </el-form-item>
      </el-form>
      <div v-if="verifyResult !== null" class="result-block">
        <el-alert
          :title="verifySuccess ? '验证通过' : '验证未通过'"
          :type="verifySuccess ? 'success' : 'error'"
          :closable="false"
          style="margin-bottom:8px;"
        />
        <el-button link type="primary" @click="showVerifyRaw = !showVerifyRaw">
          {{ showVerifyRaw ? '收起原始数据' : '查看原始数据' }}
        </el-button>
        <pre v-if="showVerifyRaw" class="json-block">{{ JSON.stringify(verifyResult, null, 2) }}</pre>
      </div>
    </div>

    <el-divider />

    <!-- ====== VC 列表 ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Document /></el-icon>
        <span>VC 列表（根据 DID 查看）</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form :inline="true" @submit.prevent="handleListVcs">
        <el-form-item label="持有者 DID">
          <el-input v-model="listHolderDid" placeholder="可选" style="width: 300px;" />
        </el-form-item>
        <el-form-item label="颁发者 DID">
          <el-input v-model="listIssuerDid" placeholder="可选" style="width: 300px;" />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleListVcs" :loading="listLoading">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="vcList" stripe style="width: 100%" max-height="400">
        <el-table-column label="VC ID" prop="vcId" min-width="240" align="center" show-overflow-tooltip />
        <el-table-column label="颁发者" prop="issuerDid" min-width="260" align="center" show-overflow-tooltip />
        <el-table-column label="持有者" prop="holderDid" min-width="260" align="center" show-overflow-tooltip />
        <el-table-column label="凭证类型" prop="credentialType" width="180" align="center" />
        <el-table-column label="验证状态" prop="verifyStatus" width="120" align="center" />
        <el-table-column label="创建时间" prop="createdAt" width="200" align="center" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="140" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleViewVcDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ====== VC 详情 Dialog ====== -->
    <el-dialog v-model="vcDetailVisible" title="VC 详情" width="60%">
      <pre class="json-block">{{ JSON.stringify(vcDetailData, null, 2) }}</pre>
      <template #footer>
        <el-button @click="vcDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { vcIssue, vcVerify, listVcs } from '../../api/goSdk.js'

// ---- 颁发 VC ----
const issueLoading = ref(false)
const issueResult = ref<any>(null)
const showIssueRaw = ref(false)
const issueForm = ref({
  issuerDid: '',
  issuerPrivateKey: '',
  issuerPublicKeyHex: '',
  holderDid: '',
  credentialType: '',
  credentialSubjectStr: '',
})

const handleIssue = async () => {
  const f = issueForm.value
  if (!f.issuerDid || !f.issuerPrivateKey || !f.holderDid || !f.credentialType) {
    ElMessage.warning('请填写必要字段（颁发者DID、私钥、持有者DID、凭证类型）')
    return
  }
  let credentialSubject = {}
  if (f.credentialSubjectStr.trim()) {
    try {
      credentialSubject = JSON.parse(f.credentialSubjectStr)
    } catch {
      ElMessage.error('凭证内容不是合法的 JSON')
      return
    }
  }
  issueLoading.value = true
  issueResult.value = null
  showIssueRaw.value = false
  try {
    const body: any = {
      issuerDid: f.issuerDid,
      issuerPrivateKey: f.issuerPrivateKey,
      holderDid: f.holderDid,
      credentialType: f.credentialType,
      credentialSubject,
    }
    if (f.issuerPublicKeyHex) body.issuerPublicKeyHex = f.issuerPublicKeyHex
    const res = await vcIssue(body)
    const resp = res.data
    if (resp.code === 0) {
      issueResult.value = resp.data
      ElMessage.success('VC 颁发成功')
    } else {
      ElMessage.error(resp.message || '颁发失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    issueLoading.value = false
  }
}

// ---- 验证 VC ----
const verifyLoading = ref(false)
const verifyVcStr = ref('')
const verifyResult = ref<any>(null)
const verifySuccess = ref(false)
const showVerifyRaw = ref(false)

const handleVerify = async () => {
  if (!verifyVcStr.value.trim()) {
    ElMessage.warning('请粘贴 VC JSON')
    return
  }
  let vc: any
  try {
    vc = JSON.parse(verifyVcStr.value)
  } catch {
    ElMessage.error('VC 不是合法的 JSON')
    return
  }
  verifyLoading.value = true
  verifyResult.value = null
  showVerifyRaw.value = false
  try {
    const res = await vcVerify(vc)
    const resp = res.data
    verifyResult.value = resp.data
    if (resp.code === 0) {
      verifySuccess.value = !!(resp.data?.valid ?? resp.data?.verified ?? true)
      ElMessage.success('验证请求完成')
    } else {
      verifySuccess.value = false
      ElMessage.error(resp.message || '验证失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    verifyLoading.value = false
  }
}

// ---- VC 列表 ----
const listHolderDid = ref('')
const listIssuerDid = ref('')
const vcList = ref<any[]>([])
const listLoading = ref(false)
const vcDetailVisible = ref(false)
const vcDetailData = ref<any>(null)

const handleListVcs = async () => {
  listLoading.value = true
  try {
    const res = await listVcs(listHolderDid.value.trim() || undefined, listIssuerDid.value.trim() || undefined)
    const resp = res.data
    if (resp.code === 0) {
      vcList.value = resp.data?.items || []
    } else {
      ElMessage.error(resp.message || '获取列表失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    listLoading.value = false
  }
}

const handleViewVcDetail = (row: any) => {
  vcDetailData.value = row
  vcDetailVisible.value = true
}

onMounted(() => {
  handleListVcs()
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

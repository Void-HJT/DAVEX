<template>
  <el-container direction="vertical">
    <!-- ====== 1. 查看 Group ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Histogram /></el-icon>
        <span>查看 Group</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form :inline="true" @submit.prevent="handleQueryGroup">
        <el-form-item label="Group ID">
          <el-input v-model="queryGroupId" placeholder="请输入 Group ID" style="width: 300px;" />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleQueryGroup" :loading="queryGroupLoading">查询 Group</el-button>
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleListGroups" :loading="listGroupsLoading">刷新列表</el-button>
        </el-form-item>
      </el-form>
      <!-- 查询结果 -->
      <div v-if="queryGroupResult" class="result-block">
        <el-button link type="primary" @click="showGroupRaw = !showGroupRaw">
          {{ showGroupRaw ? '收起原始数据' : '查看查询结果原始数据' }}
        </el-button>
        <pre v-if="showGroupRaw" class="json-block">{{ JSON.stringify(queryGroupResult, null, 2) }}</pre>
      </div>
      <!-- 列表 -->
      <el-table :data="groupList" stripe style="width: 100%; margin-top:10px;" max-height="300">
        <el-table-column label="Group ID" prop="groupId" min-width="180" align="center" show-overflow-tooltip />
        <el-table-column label="Group Name" prop="groupName" width="160" align="center" />
        <el-table-column label="颁发者 DID" prop="issuerDid" min-width="280" align="center" show-overflow-tooltip />
        <el-table-column label="凭证类型" prop="credentialType" width="160" align="center" />
        <el-table-column label="成员数" prop="memberCount" width="100" align="center" />
        <el-table-column label="最小环大小" prop="minRingSize" width="120" align="center" />
        <el-table-column label="创建时间" prop="createdAt" width="200" align="center" show-overflow-tooltip />
      </el-table>
    </div>

    <el-divider />

    <!-- ====== 2. 建立 Group ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Plus /></el-icon>
        <span>建立 Group</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form :model="createGroupForm" label-width="160px" style="max-width: 650px;">
        <el-form-item label="颁发者 DID">
          <el-input v-model="createGroupForm.IssuerDid" placeholder="did:mychain:xxxx" />
        </el-form-item>
        <el-form-item label="Group ID">
          <el-input v-model="createGroupForm.GroupId" placeholder="例如 group12345" />
        </el-form-item>
        <el-form-item label="Group Name">
          <el-input v-model="createGroupForm.GroupName" placeholder="群组名称" />
        </el-form-item>
        <el-form-item label="凭证类型">
          <el-input v-model="createGroupForm.CredentialType" placeholder="例如 IdentityCredential" />
        </el-form-item>
        <el-form-item label="属性策略">
          <el-input v-model="createGroupForm.AttributePolicy" placeholder="例如 {age>=18}" />
        </el-form-item>
        <el-form-item label="最小环大小">
          <el-input-number v-model="createGroupForm.MinRingSize" :min="2" :max="100" />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleCreateGroup" :loading="createGroupLoading">创建 Group</el-button>
        </el-form-item>
      </el-form>
      <div v-if="createGroupResult" class="result-block">
        <el-alert title="Group 创建成功" type="success" :closable="false" />
      </div>
    </div>

    <el-divider />

    <!-- ====== 3. Group 加入成员 ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><UserFilled /></el-icon>
        <span>Group 加入成员</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form :model="addMemberForm" label-width="160px" style="max-width: 700px;">
        <el-form-item label="Group ID">
          <el-input v-model="addMemberForm.GroupID" placeholder="例如 group12345" />
        </el-form-item>
        <el-form-item label="成员公钥">
          <el-input v-model="addMemberForm.PublicKeyHex" placeholder="0x04..." type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleAddMember" :loading="addMemberLoading">添加成员</el-button>
        </el-form-item>
      </el-form>
      <div v-if="addMemberResult" class="result-block">
        <el-alert title="成员添加成功" type="success" :closable="false" />
      </div>
    </div>

    <el-divider />

    <!-- ====== 4. 获取 VP (隐私 VP 生成) ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><Lock /></el-icon>
        <span>获取 VP（隐私 VP 生成）</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form :model="generateVpForm" label-width="160px" style="max-width: 700px;">
        <el-form-item label="持有者私钥">
          <el-input v-model="generateVpForm.holderPrivateKeyHex" placeholder="0x..." />
        </el-form-item>
        <el-form-item label="持有者公钥">
          <el-input v-model="generateVpForm.holderPublicKeyHex" placeholder="0x04..." />
        </el-form-item>
        <el-form-item label="Group ID">
          <el-input v-model="generateVpForm.groupId" placeholder="例如 group12345" />
        </el-form-item>
        <el-form-item label="Challenge">
          <el-input v-model="generateVpForm.challenge" placeholder="例如 random-challenge" />
        </el-form-item>
        <el-form-item label="凭证类型">
          <el-input v-model="generateVpForm.credentialType" placeholder="例如 IdentityCredential" />
        </el-form-item>
        <el-form-item label="颁发者 DID">
          <el-input v-model="generateVpForm.issuerDid" placeholder="did:mychain:xxxx" />
        </el-form-item>
        <el-form-item label="Claims (JSON数组)">
          <el-input
            v-model="generateVpForm.claimsStr"
            type="textarea"
            :rows="4"
            placeholder='[{"attribute":"age","operator":">=","value":"18"}]'
          />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleGenerateVp" :loading="generateVpLoading">生成隐私 VP</el-button>
        </el-form-item>
      </el-form>
      <div v-if="generateVpResult" class="result-block">
        <el-alert title="隐私 VP 生成成功" type="success" :closable="false" style="margin-bottom:8px;" />
        <el-button link type="primary" @click="showGenerateVpRaw = !showGenerateVpRaw">
          {{ showGenerateVpRaw ? '收起原始数据' : '查看原始数据' }}
        </el-button>
        <pre v-if="showGenerateVpRaw" class="json-block">{{ JSON.stringify(generateVpResult, null, 2) }}</pre>
      </div>
    </div>

    <el-divider />

    <!-- ====== 5. 验证 VP (隐私 VP 验证) ====== -->
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><CircleCheck /></el-icon>
        <span>验证 VP（隐私 VP 链上验证）</span>
      </div>
    </el-header>
    <div class="section-body">
      <el-form label-width="160px" style="max-width: 700px;">
        <el-form-item label="Privacy VP JSON">
          <el-input
            v-model="verifyVpStr"
            type="textarea"
            :rows="10"
            placeholder="请粘贴完整的 PrivacyVP JSON 对象"
          />
        </el-form-item>
        <el-form-item label="Challenge">
          <el-input v-model="verifyVpChallenge" placeholder="例如 random-challenge" />
        </el-form-item>
        <el-form-item>
          <el-button class="default-button" @click="handleVerifyVp" :loading="verifyVpLoading">验证隐私 VP</el-button>
        </el-form-item>
      </el-form>
      <div v-if="verifyVpResult !== null" class="result-block">
        <el-alert
          :title="verifyVpSuccess ? '验证通过' : '验证未通过'"
          :type="verifyVpSuccess ? 'success' : 'error'"
          :closable="false"
          style="margin-bottom:8px;"
        />
        <el-button link type="primary" @click="showVerifyVpRaw = !showVerifyVpRaw">
          {{ showVerifyVpRaw ? '收起原始数据' : '查看原始数据' }}
        </el-button>
        <pre v-if="showVerifyVpRaw" class="json-block">{{ JSON.stringify(verifyVpResult, null, 2) }}</pre>
      </div>
    </div>
  </el-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  privacyGroupCreate,
  privacyGroupMember,
  privacyGroupQuery,
  listGroups,
  privacyVpGenerate,
  privacyVpVerify,
} from '../../api/goSdk.js'

// ==================== 1. 查看 Group ====================
const queryGroupId = ref('')
const queryGroupLoading = ref(false)
const queryGroupResult = ref<any>(null)
const showGroupRaw = ref(false)
const groupList = ref<any[]>([])
const listGroupsLoading = ref(false)

const handleQueryGroup = async () => {
  if (!queryGroupId.value.trim()) {
    ElMessage.warning('请输入 Group ID')
    return
  }
  queryGroupLoading.value = true
  queryGroupResult.value = null
  showGroupRaw.value = false
  try {
    const res = await privacyGroupQuery(queryGroupId.value.trim())
    const resp = res.data
    if (resp.code === 0) {
      queryGroupResult.value = resp.data
      ElMessage.success('查询成功')
    } else {
      ElMessage.error(resp.message || '查询失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    queryGroupLoading.value = false
  }
}

const handleListGroups = async () => {
  listGroupsLoading.value = true
  try {
    const res = await listGroups(queryGroupId.value.trim() || undefined)
    const resp = res.data
    if (resp.code === 0) {
      groupList.value = resp.data?.items || []
    } else {
      ElMessage.error(resp.message || '获取列表失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    listGroupsLoading.value = false
  }
}

// ==================== 2. 建立 Group ====================
const createGroupForm = ref({
  IssuerDid: '',
  GroupId: '',
  GroupName: '',
  CredentialType: '',
  AttributePolicy: '',
  MinRingSize: 3,
})
const createGroupLoading = ref(false)
const createGroupResult = ref<any>(null)

const handleCreateGroup = async () => {
  const f = createGroupForm.value
  if (!f.IssuerDid || !f.GroupId || !f.GroupName) {
    ElMessage.warning('请填写必要字段（颁发者DID、Group ID、Group Name）')
    return
  }
  createGroupLoading.value = true
  createGroupResult.value = null
  try {
    const res = await privacyGroupCreate(f)
    const resp = res.data
    if (resp.code === 0) {
      createGroupResult.value = resp.data
      ElMessage.success('Group 创建成功')
      handleListGroups()
    } else {
      ElMessage.error(resp.message || '创建失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    createGroupLoading.value = false
  }
}

// ==================== 3. Group 加入成员 ====================
const addMemberForm = ref({
  GroupID: '',
  PublicKeyHex: '',
})
const addMemberLoading = ref(false)
const addMemberResult = ref<any>(null)

const handleAddMember = async () => {
  const f = addMemberForm.value
  if (!f.GroupID || !f.PublicKeyHex) {
    ElMessage.warning('请填写 Group ID 和成员公钥')
    return
  }
  addMemberLoading.value = true
  addMemberResult.value = null
  try {
    const res = await privacyGroupMember(f)
    const resp = res.data
    if (resp.code === 0) {
      addMemberResult.value = resp.data
      ElMessage.success('成员添加成功')
    } else {
      ElMessage.error(resp.message || '添加失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    addMemberLoading.value = false
  }
}

// ==================== 4. 获取 VP ====================
const generateVpForm = ref({
  holderPrivateKeyHex: '',
  holderPublicKeyHex: '',
  groupId: '',
  challenge: '',
  credentialType: '',
  issuerDid: '',
  claimsStr: '',
})
const generateVpLoading = ref(false)
const generateVpResult = ref<any>(null)
const showGenerateVpRaw = ref(false)

const handleGenerateVp = async () => {
  const f = generateVpForm.value
  if (!f.holderPrivateKeyHex || !f.holderPublicKeyHex || !f.groupId) {
    ElMessage.warning('请填写必要字段（私钥、公钥、Group ID）')
    return
  }
  let claims: any[] = []
  if (f.claimsStr.trim()) {
    try {
      claims = JSON.parse(f.claimsStr)
    } catch {
      ElMessage.error('Claims 不是合法的 JSON 数组')
      return
    }
  }
  generateVpLoading.value = true
  generateVpResult.value = null
  showGenerateVpRaw.value = false
  try {
    const body: any = {
      holderPrivateKeyHex: f.holderPrivateKeyHex,
      holderPublicKeyHex: f.holderPublicKeyHex,
      groupId: f.groupId,
      challenge: f.challenge,
      claims,
      credentialType: f.credentialType,
      issuerDid: f.issuerDid,
    }
    const res = await privacyVpGenerate(body)
    const resp = res.data
    if (resp.code === 0) {
      generateVpResult.value = resp.data
      ElMessage.success('隐私 VP 生成成功')
    } else {
      ElMessage.error(resp.message || '生成失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    generateVpLoading.value = false
  }
}

// ==================== 5. 验证 VP ====================
const verifyVpStr = ref('')
const verifyVpChallenge = ref('')
const verifyVpLoading = ref(false)
const verifyVpResult = ref<any>(null)
const verifyVpSuccess = ref(false)
const showVerifyVpRaw = ref(false)

const handleVerifyVp = async () => {
  if (!verifyVpStr.value.trim()) {
    ElMessage.warning('请粘贴 Privacy VP JSON')
    return
  }
  let privacyVP: any
  try {
    privacyVP = JSON.parse(verifyVpStr.value)
  } catch {
    ElMessage.error('Privacy VP 不是合法的 JSON')
    return
  }
  verifyVpLoading.value = true
  verifyVpResult.value = null
  showVerifyVpRaw.value = false
  try {
    const body = {
      PrivacyVP: privacyVP,
      Challenge: verifyVpChallenge.value || '',
    }
    const res = await privacyVpVerify(body)
    const resp = res.data
    verifyVpResult.value = resp.data
    if (resp.code === 0) {
      verifyVpSuccess.value = !!(resp.data?.valid ?? resp.data?.verified ?? true)
      ElMessage.success('验证请求完成')
    } else {
      verifyVpSuccess.value = false
      ElMessage.error(resp.message || '验证失败')
    }
  } catch (e: any) {
    ElMessage.error('请求失败：' + (e.message || e))
  } finally {
    verifyVpLoading.value = false
  }
}

onMounted(() => {
  handleListGroups()
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

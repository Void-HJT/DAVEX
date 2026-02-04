<template>
  <el-container class="verdict-container">
    <!-- 筛选条件区域 -->
    <el-header class="filter-header">
      <div class="icon-text">
        <el-icon><Filter /></el-icon>
        <span>类案筛选条件</span>
      </div>
    </el-header>

    <el-main class="filter-main">
      <el-form
          :model="filterForm"
          class="filter-form"
          label-width="120px"
      >
        <!-- 新增Agent选择下拉框 -->
        <el-form-item label="目标Agent" required>
          <el-select
              v-model="agentId"
              placeholder="请选择目标Agent"
              clearable
              class="agent-select"
              @change="handleSelectAgent"
          >
            <el-option
                v-for="item in agents"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <!-- 新增：查询文件上传框 -->
        <el-form-item label="查询文件" required>
          <el-upload
              class="query-file-upload"
              :auto-upload="false"
              :on-change="handleFileChange"
              :file-list="fileList"
              accept=".txt"
              drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处上传，或<em>点击上传</em>
            </div>
            <div class="el-upload__tip" slot="tip">
              仅支持 .txt 格式文本文件
            </div>
          </el-upload>
        </el-form-item>

        <!-- 判决时间范围 -->
        <el-form-item label="判决时间范围">
          <el-date-picker
              v-model="filterForm.judgeTimeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="date-picker"
          />
        </el-form-item>

        <!-- 判决书类型 -->
        <el-form-item label="判决书类型">
          <el-input
              v-model="filterForm.judgeType"
              placeholder="请输入判决书类型（如：刑事判决书）"
              clearable
          />
        </el-form-item>

        <!-- 判决地点 -->
        <el-form-item label="判决地点">
          <el-input
              v-model="filterForm.judgeDistrict"
              placeholder="请输入判决地点（如：北京市）"
              clearable
          />
        </el-form-item>

        <!-- 案由 -->
        <el-form-item label="案由">
          <el-input
              v-model="filterForm.judgeCause"
              placeholder="请输入案由（如：贪污）"
              clearable
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item class="form-button-group">
          <el-button
              class="default-button"
              @click="resetFilterForm"
          >
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
          <el-button
              class="start-button"
              @click="handleTargetPreprocess"
              :loading="preprocessLoading"
              :disabled="!agentId || !selectedFile"
          >
            <el-icon><Search /></el-icon> 类案检索
          </el-button>
        </el-form-item>
      </el-form>
    </el-main>
  </el-container>

  <!-- 预处理结果对话框 -->
  <el-dialog
      v-model="preprocessDialogVisible"
      :title="preprocessSuccess ? '处理成功' : '处理失败'"
      width="30%"
      :close-on-click-modal="false"
  >
    <div class="dialog-content">
      <el-icon :class="preprocessSuccess ? 'success-icon' : 'fail-icon'">
        <template v-if="preprocessSuccess"><Check /></template>
        <template v-else><Close /></template>
      </el-icon>
      <p class="result-message">{{ preprocessMessage }}</p>
      <!-- 成功时显示检索结果 -->
      <div v-if="preprocessSuccess && embFilePath" class="emb-path-container">
        <span class="path-label">任务ID：</span>
        <el-input
            v-model="embFilePath"
            readonly
            class="emb-path-input"
        />
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <router-link to="/verdictResult/query">
          <el-button class="default-button">
            查看结果管理区
          </el-button>
        </router-link>
        <el-button
            class="close-button"
            @click="preprocessDialogVisible = false"
        >
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { getAgent } from '../../api/testDve.js'
import { ref, reactive, onMounted } from "vue";
// 替换为sendAndCompute接口
import { sendAndCompute } from "../../api/verdict.js";
import { addOperationHistory } from '../../api/operationHistory.js'
import {
  Filter,
  Loading,
  RefreshLeft,
  Check,
  Close,
  UploadFilled // 新增上传图标
} from "@element-plus/icons-vue";
import { UploadProps } from "element-plus"; // 上传组件类型定义

onMounted(() => {
  getAgentMethod() // 页面加载时获取Agent列表
})

// Agent相关状态
const agents = ref([]) // Agent列表
const agentId = ref('') // 选中的AgentID
const agentLoading = ref(false) // Agent下拉框加载状态

// 文件上传相关状态
const fileList = ref<UploadProps['fileList']>([]) // 上传文件列表
const selectedFile = ref<File | null>(null) // 选中的文件对象

// 筛选表单数据（映射后端VerdictFilterDTO）
const filterForm = reactive({
  judgeTimeRange: [] as (string | undefined)[], // 日期范围：支持空数组、单值、双值
  judgeType: "" as string | undefined, // 判决书类型（可选）
  judgeDistrict: "" as string | undefined, // 判决地点（可选）
  judgeCause: "" as string | undefined // 案由（可选）
});

// 预处理状态相关
const preprocessLoading = ref(false); // 预处理按钮加载状态
const preprocessDialogVisible = ref(false); // 结果对话框显示状态
const preprocessSuccess = ref(false); // 预处理是否成功
const preprocessMessage = ref(""); // 预处理结果消息
const embFilePath = ref(""); // 生成的emb文件路径（替换原pkl路径）

/**
 * 获取Agent列表
 */
const getAgentMethod = async () => {
  try {
    agentLoading.value = true;
    const res = await getAgent();
    // 适配Agent列表格式（value为AgentID，label为显示文本）
    agents.value = res.data.body.data.map(item => ({
      value: item.uid,
      label: item.uid // 若有Agent名称字段，可改为 item.name || item.uid
    }));
  } catch (error) {
    console.error("获取Agent列表失败：", error);
    agents.value = [];
    preprocessMessage.value = "获取Agent列表失败，请刷新页面重试";
    preprocessSuccess.value = false;
    preprocessDialogVisible.value = true;
  } finally {
    agentLoading.value = false;
  }
};

/**
 * 选择Agent回调（可选：如需额外处理Agent切换逻辑）
 */
const handleSelectAgent = (value: string) => {
  console.log("选中的AgentID：", value);
  agentId.value = value;
  // 可添加Agent切换后的额外逻辑（如清空之前的筛选结果、加载该Agent的专属配置等）
};

/**
 * 处理文件上传变更
 */
const handleFileChange = (file: UploadProps['fileList'][0]) => {
  // 清空原有文件列表，只保留当前选中文件
  fileList.value = [file];
  // 保存文件对象（用于接口传递）
  selectedFile.value = file.raw as File;
  console.log("选中的查询文件：", selectedFile.value?.name);
};

/**
 * 重置筛选表单
 */
const resetFilterForm = () => {
  // 重置筛选条件
  filterForm.judgeTimeRange = [];
  filterForm.judgeType = undefined;
  filterForm.judgeDistrict = undefined;
  filterForm.judgeCause = undefined;
  // 重置文件上传
  fileList.value = [];
  selectedFile.value = null;
  // 可选：是否重置Agent选择（根据业务需求决定）
  // agentId.value = '';
};

/**
 * 处理目标集合预处理+生成Emb（调用sendAndCompute接口）
 */
const handleTargetPreprocess = async () => {
  try {
    // 1. 校验参数
    if (!agentId.value) {
      preprocessMessage.value = "请先选择目标Agent";
      preprocessSuccess.value = false;
      preprocessDialogVisible.value = true;
      return;
    }
    if (!selectedFile.value) {
      preprocessMessage.value = "请先上传查询文件";
      preprocessSuccess.value = false;
      preprocessDialogVisible.value = true;
      return;
    }

    preprocessLoading.value = true;

    // 2. 构造筛选参数（适配后端VerdictFilterDTO格式）
    const filterParams = {
      judgeTimeStart: filterForm.judgeTimeRange[0], // 开始时间（未选择则为undefined）
      judgeTimeEnd: filterForm.judgeTimeRange[1], // 结束时间（未选择则为undefined）
      judgeType: filterForm.judgeType?.trim() || undefined, // 去空后传递
      judgeDistrict: filterForm.judgeDistrict?.trim() || undefined,
      judgeCause: filterForm.judgeCause?.trim() || undefined
    };

    // 3. 调用sendAndCompute接口：传递agentId、筛选条件、查询文件
    const res = await sendAndCompute({
      agentId: agentId.value,
      filterParams: filterParams,
      inputFile: selectedFile.value
    });

    // 4. 处理接口返回结果（适配后端统一Body格式）
    if (res.data.code === 1 && res.data.data) {
      preprocessSuccess.value = true;
      preprocessMessage.value = `任务执行成功，请到结果管理区查看执行结果！`;
      embFilePath.value = res.data.data;
      // 记录操作历史
      await addOperationHistory({
        agentId: agentId.value,
        operationType: '类案检索',
        operationObject: selectedFile.value?.name || '查询文件',
        result: '成功',
        remark: `任务ID: ${res.data.data}，目标代理: ${agentId.value}`
      });
    } else {
      preprocessSuccess.value = false;
      preprocessMessage.value = res.data.message || "处理失败，请重试！";
      // 记录失败操作
      await addOperationHistory({
        agentId: agentId.value,
        operationType: '类案检索',
        operationObject: selectedFile.value?.name || '查询文件',
        result: '失败',
        remark: res.data.message || '类案检索失败'
      });
    }
  } catch (error) {
    console.error("目标集合预处理+Emb生成异常：", error);
    preprocessSuccess.value = false;
    preprocessMessage.value = "系统异常，处理失败，请检查日志或联系管理员！";
    // 记录失败操作
    await addOperationHistory({
      agentId: agentId.value,
      operationType: '类案检索',
      operationObject: selectedFile.value?.name || '查询文件',
      result: '失败',
      remark: error.message || '系统异常'
    });
  } finally {
    preprocessLoading.value = false;
    preprocessDialogVisible.value = true; // 显示结果对话框
  }
};
</script>

<style scoped>
.verdict-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.filter-header {
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  padding: 12px 20px;
  display: flex;
  align-items: center;
}

.icon-text {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

.icon-text el-icon {
  margin-right: 8px;
  color: #409efc;
}

.filter-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.filter-form {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* Agent选择下拉框样式 */
.agent-select {
  width: 100%;
}

/* 查询文件上传样式 */
.query-file-upload {
  width: 100%;
}

.date-picker {
  width: 100%;
}

.form-button-group {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.default-button {
  margin-right: 10px;
  background-color: #f5f7fa;
  color: #1f2937;
}

.start-button {
  background-color: #409efc;
  color: #fff;
}

.start-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.start-button:hover:not(:disabled) {
  background-color: #3586d4;
}

/* 结果对话框样式 */
.dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.success-icon {
  color: #10b981;
  font-size: 24px;
  margin-bottom: 12px;
}

.fail-icon {
  color: #ef4444;
  font-size: 24px;
  margin-bottom: 12px;
}

.result-message {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 16px;
  text-align: center;
}

/* Emb路径样式（替换原pkl路径） */
.emb-path-container {
  width: 100%;
  margin-top: 8px;
}

.path-label {
  display: block;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.emb-path-input {
  font-size: 13px;
  background-color: #f9fafb;
  color: #1f2937;
}

.dialog-footer {
  display: flex;
  justify-content: center;
}

.close-button {
  background-color: #409efc;
  color: #fff;
}

.close-button:hover {
  background-color: #3586d4;
}
</style>
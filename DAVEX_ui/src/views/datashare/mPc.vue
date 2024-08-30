<template>
  <!-- <div>
    <el-form
      :model="compileParameter"
      label-width="auto"
      style="max-width: 600px"
    >
      <el-form-item label="编译参数名">
        <el-input v-model="compileParameter.name" />
      </el-form-item>
      <el-form-item label="参数">
        <el-select
          v-model="compileParameter.limitType"
          placeholder="设置参数内容"
        >
          <el-option label="NUM" value="NUM" />
          <el-option label="STRING" value="STRING" />
          <el-option label="ENUM" value="ENUM" />
        </el-select>
      </el-form-item>

      <el-form-item label="禁止为空">
        <el-switch v-model="compileParameter.required" />
      </el-form-item>
      <el-form-item label="自动填充">
        <el-switch v-model="compileParameter.auto" />
      </el-form-item>
      <el-form-item label="参数类型">
        <el-select
          v-model="compileParameter.parameterType"
          placeholder="选择参数类型"
        >
          <el-option label="位置参数" value="POS" />
          <el-option label="FLAG参数" value="FLAG" />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="compileParameter.parameterType === 'FLAG'"
        label="FLAG参数"
      >
        <el-input
          v-model="compileParameter.posORflag"
          placeholder="输入FLAG参数"
        ></el-input>
      </el-form-item>

      <el-form-item label="参数描述">
        <el-input v-model="compileParameter.description" type="textarea" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="addcompileRarameter">
          增加编译参数
        </el-button>
      </el-form-item>
    </el-form>
  </div> -->
  <div>
    <el-form :model="mpcTaskInfo" label-width="auto" style="max-width: 600px">
      <el-form-item label="mpc任务名">
        <el-input v-model="mpcTaskInfo.name" />
      </el-form-item>
      <!-- <el-button>上传mpc文件</el-button> -->
      <el-button @click="addcompileRarameter">增加编译参数</el-button>
    </el-form>
  </div>
  <div>
    <el-table
      :data="mpcTaskInfo.compileParameters"
      style="width: 100%; margin-top: 20px"
      stripe
      border
      height="300px"
      max-height="300px"
    >
      <el-table-column
        type="index"
        label="序号"
        width="100"
        align="center"
      ></el-table-column>
      <!-- 列：编译参数名 -->
      <el-table-column
        prop="name"
        label="编译参数名"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-input v-model="scope.row.name" />
        </template>
      </el-table-column>

      <!-- 列：参数类型 -->
      <el-table-column
        prop="limitType"
        label="参数限制"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-select v-model="scope.row.limitType" placeholder="设置参数内容">
            <el-option label="NUM" value="NUM" />
            <el-option label="STRING" value="STRING" />
            <el-option label="ENUM" value="ENUM" />
          </el-select>
        </template>
      </el-table-column>

      <!-- 列：禁止为空 -->
      <el-table-column
        prop="required"
        label="禁止为空"
        width="100"
        align="center"
      >
        <template #default="scope">
          <el-switch v-model="scope.row.required" />
        </template>
      </el-table-column>

      <!-- 列：自动填充 -->
      <el-table-column prop="auto" label="自动填充" width="100" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.auto" />
        </template>
      </el-table-column>

      <!-- 列：参数类型 -->
      <el-table-column
        prop="parameterType"
        label="参数类型"
        width="150"
        align="center"
      >
        <template #default="scope">
          <el-select
            v-model="scope.row.parameterType"
            placeholder="设置参数类型"
            @change="() => handleParameterTypeChange(scope.row)"
          >
            <el-option label="位置参数" value="POS" />
            <el-option label="FLAG参数" value="FLAG" />
          </el-select>
        </template>
      </el-table-column>

      <!-- 列：FLAG参数 -->
      <el-table-column
        prop="posORflag"
        label="参数详情"
        width="200"
        align="center"
      >
        <template #default="scope">
          <span v-if="scope.row.parameterType === 'POS'">
            当前是第 {{ scope.row.posORflag }} 位
          </span>
          <el-input
            v-else
            v-model="scope.row.posORflag"
            placeholder="输入FLAG参数"
          />
        </template>
      </el-table-column>

      <!-- 列：参数描述 -->
      <el-table-column
        prop="description"
        label="参数描述"
        width="300"
        align="center"
      >
        <template #default="scope">
          <el-input v-model="scope.row.description" type="textarea" />
        </template>
      </el-table-column>

      <!-- 操作列：删除按钮 -->
      <el-table-column label="操作" mid-width="100" align="center">
        <template #default="scope">
          <el-button
            type="danger"
            @click="removeCompileParameter(scope.$index)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { reactive } from 'vue'
onMounted(() => {
  addcompileRarameter()
})

function createParameter() {
  return {
    name: '',
    limit: {},
    required: true,
    auto: false,
    limitType: '',
    posORflag: '',
    description: '',
    parameterType: '',
  }
}

const compileParameter = reactive({
  name: '',
  limit: {},
  required: true,
  auto: false,
  limitType: '',
  posORflag: 0,
  description: '',
  parameterType: '',
})

const runtimeParameter = reactive({
  name: '',
  limit: {},
  required: true,
  auto: false,
  limitType: '',
  posORflag: 0,
  description: '',
  parameterType: '',
})

const mpcTaskInfo = ref({
  centerId: 1,
  name: '',
  compileParameters: [],
  runtimeParameters: [],
})

const addcompileRarameter = () => {
  mpcTaskInfo.value.compileParameters.push(createParameter())
  console.log(mpcTaskInfo.value)
}

const removeCompileParameter = (index) => {
  mpcTaskInfo.value.compileParameters.splice(index, 1)
  updatePosParameters() // 删除后更新位置参数
}

const handleParameterTypeChange = (row) => {
  if (row.parameterType === 'POS') {
    updatePosParameters() // 当选择位置参数时，更新位置参数的值
  } else if (row.parameterType === 'FLAG') {
    updatePosParameters()
    row.posORflag = '' // 允许手动输入 FLAG 参数
  }
}

// 更新所有位置参数的自增值
const updatePosParameters = () => {
  let posIndex = 1
  mpcTaskInfo.value.compileParameters.forEach((param) => {
    if (param.parameterType === 'POS') {
      param.posORflag = posIndex++
    }
  })
}
</script>

<style scoped></style>

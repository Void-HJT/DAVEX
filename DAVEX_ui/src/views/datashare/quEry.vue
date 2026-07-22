<template>
  <el-container>
    <div style="display: flex; gap: 10px; align-items: center;">
      <span style="display: block; margin-bottom: 10px;">代理</span>
      <el-select v-model="agentId" placeholder="选择代理" @change="handleSelectAgent">
        <el-option
            v-for="item in agents"
            :key="item.value"
            :label="item.label"
            :value="item.value"
        />
      </el-select>
      <div>
        <el-button
            class="default-button"
            @click="addDialogVisible = true"
        >
          <el-icon><Plus /></el-icon> 新增数据库
        </el-button>
      </div>
    </div>
    <el-header class="custom-header">
      <div class="icon-text">
        <el-icon><DataBoard /></el-icon>
        <span>接入数据库表</span>
      </div>
    </el-header>
    <el-table
      :data="databaseData"
      height="200"
      max-height="200"
    >
      <el-table-column
        label="数据库id"
        prop="uid"
        width="90"
        align="center"
      ></el-table-column>
      <el-table-column
        label="代理id"
        prop="agentId"
        width="90"
        align="center"
      ></el-table-column>
      <el-table-column
        label="数据库名"
        prop="name"
        width="180"
        align="center"
      ></el-table-column>
      <el-table-column
        label="数据库种类"
        prop="type"
        width="180"
        align="center"
      ></el-table-column>
      <el-table-column
        label="数据库地址"
        prop="connection"
        width="300"
        align="center"
      ></el-table-column>
      <el-table-column
        label="详细信息"
        prop="description"
        width="250"
        align="center"
      ></el-table-column>
      <el-table-column
        fixed="right"
        label="操作"
        min-width="60"
        header-align="center"
        align="center"
      >
        <template v-slot="scope">
          <div
            style="
              display: flex;
              justify-content: center;
              align-items: center;
            "
          >
            <el-button
                class="small-default-button"
              @click="handleViewDatabaseTables(scope.row)"
            >
              <el-icon><Memo /></el-icon> 查看数据库表
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="databaseControlDialogVisible"
      title="数据库表"
      width="800"
    >
      <el-table
        :data="databaseTableData"
        style="width: 100%"
        stripe
        height="200"
        max-height="200"
      >
        <el-table-column
          label="代理id"
          prop="agentId"
          width="80"
          align="center"
        ></el-table-column> 
        <el-table-column
          label="表名"
          prop="name"
          width="180"
          align="center"
        ></el-table-column>
        <el-table-column
          label="简介"
          prop="description"
          width="180"
          align="center"
        ></el-table-column>
        <el-table-column
          fixed="right"
          label="查看"
          min-width="60"
          header-align="center"
          align="center"
        >
          <template v-slot="scope">
            <div
              style="
                display: flex;
                justify-content: center;
                align-items: center;
              "
            >
              <el-button
                  class="small-default-button"
                @click="handleViewDatabaseTableSchemaExample(scope.row)"
              >
                <el-icon><Memo /></el-icon> 表格式
              </el-button>
              <el-button
                  class="small-default-button"
                @click="handleViewDatabaseTableExample(scope.row)"
              >
                <el-icon><Memo /></el-icon> 样例
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          label="操作"
          min-width="60"
          header-align="center"
          align="center"
        >
          <template v-slot="scope">
            <div
              style="
                display: flex;
                justify-content: center;
                align-items: center;
              "
            >
              <el-button
                  class="small-default-button"
                @click="handleViewOpenQueryDialog(scope.row)"
              >
                <el-icon><Search /></el-icon> 查询
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="exampleDialogVisible"
      title="样例内容"
      width="600"
    >
      <el-table :data="exampleTableData" style="width: 100%" stripe>
        <el-table-column
          label="行名"
          prop="column_name"
          align="center"
        ></el-table-column>
        <el-table-column
          label="值"
          prop="value"
          align="center"
        ></el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog
      v-model="schemaDialogVisible"
      title="表格式"
      width="600"
    >
      <el-table :data="schemaTableData" style="width: 100%" stripe>
        <el-table-column
          label="行名"
          prop="column_name"
          align="center"
        ></el-table-column>
        <el-table-column
          label="属性"
          prop="column_type"
          align="center"
        ></el-table-column>
        <el-table-column
          label="大小"
          prop="column_size"
          align="center"
        ></el-table-column>
      </el-table>
    </el-dialog>

    <!-- 查询条件对话框 -->
    <el-dialog
      v-model="queryDialogVisible"
      title="查询条件"
      width="800"
    >
      <el-form :model="queryForm" label-width="120px">
        <el-form-item label="表名">
          <el-input v-model="queryForm.tableName" disabled></el-input>
        </el-form-item>

        <el-form-item label="选择列">
          <el-select v-model="queryForm.columns" multiple placeholder="请选择查询的列">
            <el-option
              v-for="item in schemaTableData"
              :key="item.column_name"
              :label="item.column_name"
              :value="item.column_name"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="查询条件">
          <div v-for="(condition, index) in queryForm.conditions" :key="index" style="margin-bottom: 15px; display: flex; align-items: center;">
          <el-select v-model="condition.column" placeholder="选择列" style="flex: 1; margin-right: 10px;">
          <el-option
          v-for="item in schemaTableData"
          :key="item.column_name"
          :label="item.column_name"
          :value="item.column_name"
          ></el-option>
          </el-select>
          <el-select v-model="condition.operator" placeholder="选择操作符" style="flex: 1; margin-right: 10px;">
          <el-option label="=" value="="></el-option>
          <el-option label=">" value=">"></el-option>
          <el-option label="<" value="<"></el-option>
          <el-option label="LIKE" value="LIKE"></el-option>
          </el-select>
          <el-input v-model="condition.value" placeholder="输入值" style="flex: 2; margin-right: 10px;"></el-input>
          <el-button
              class="small-delete-button"
              @click="removeCondition(index)" >
            <el-icon><Delete /></el-icon> 删除条件
          </el-button>
          </div>
          <el-button
              class="small-default-button"
              @click="addCondition" >
            <el-icon><Edit /></el-icon> 添加条件
          </el-button>
          </el-form-item>

        <el-form-item label="排序">
          <el-select v-model="queryForm.orderBy.column" placeholder="选择排序列">
            <el-option
              v-for="item in schemaTableData"
              :key="item.column_name"
              :label="item.column_name"
              :value="item.column_name"
            ></el-option>
          </el-select>
          <el-radio-group v-model="queryForm.orderBy.order" style="margin-left: 10px;">
            <el-radio label="ASC">升序</el-radio>
            <el-radio label="DESC">降序</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="限制">
          <el-input-number v-model="queryForm.limit" placeholder="限制返回条数"></el-input-number>
        </el-form-item>

        <el-form-item label="返回">
          <el-input-number v-model="queryForm.offset" placeholder="从第几条返回"></el-input-number>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button class="start-button" @click="generateQuery">生成查询语句</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="querySuccessVisible" title="查询完成" width="30%">
    <span>{{ querySuccessMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="querySuccessVisible = false" style="margin-right: 10px">返回</el-button>
        <router-link to="/result/quEry">
          <el-button class="default-button">
            查看结果管理区
          </el-button>
        </router-link>
      </div>
    </template>
  </el-dialog>
  <el-dialog v-model="queryFailedVisible" title="正在查询" width="30%">
    <span>{{ queryFailedMessage }}</span>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="close-button" @click="queryFailedVisible = false">返回</el-button>
      </div>
    </template>
  </el-dialog>

    <!-- 新增数据库对话框 -->
    <el-dialog
        v-model="addDialogVisible"
        title="新增数据库"
        width="800"
    >
      <el-form :model="addForm" label-width="120px">
        <el-form-item label="代理">
          <el-input v-model="addForm.agentId" disabled></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="addForm.type" disabled></el-input>
        </el-form-item>
        <el-form-item label="数据库名">
          <el-input v-model="addForm.name"></el-input>
        </el-form-item>
        <el-form-item label="连接信息">
          <el-input v-model="addForm.connection"></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="addForm.username"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="addForm.password"></el-input>
        </el-form-item>
        <el-form-item label="描述信息">
          <el-input v-model="addForm.description"></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button class="start-button" @click="addDatabaseMethod">新增数据库</el-button>
      </template>
    </el-dialog>

  </el-container>
</template>


<script lang="ts" setup>
import {ref, onMounted, reactive} from 'vue'

import {getAgent} from '../../api/testDve.js'
import {
  getDatabase,
  getDatabaseTable,
  query,
  addDatabase
} from '../../api/query.js'
import {DataBoard, Delete, Download, Edit, Search, Plus} from "@element-plus/icons-vue";

//初始化
onMounted(() => {
  getAgentMethod()
})

// 控制对话框的变量
const databaseControlDialogVisible = ref(false)
const exampleDialogVisible = ref(false)
const schemaDialogVisible = ref(false)
const queryDialogVisible = ref(false)
const querySuccessVisible = ref(false)
const queryFailedVisible = ref(false)
const querySuccessMessage = ref('');
const queryFailedMessage = ref('');
const addDialogVisible = ref(false)

// 表格数据
const databaseData = ref([])
const databaseTableData = ref([])
const currentExample = ref('')
const schemaExample = ref('')
const schemaTableData = ref([])
const exampleTableData = ref([])
const agents = ref([])
const agentId = ref('')
let applicationId = 6

// 查询表单
const queryForm = ref({
  applicationId: applicationId,
  databaseId: 0,
  tableName: '',     // 从表格式中自动填充
  columns: [],       // 用户选择的列
  conditions: [],    // 查询条件
  orderBy: {
    column: '',      // 排序列
    order: 'ASC',    // 排序方式
  },
  limit: 10,         // 查询返回的记录条数
  offset: 0,         // 偏移量
});

// 新增表单
const addForm = reactive({
  get agentId() {
    return agentId.value;
  },
  name: '',
  type: 'mysql',
  connection: 'jdbc:mysql://10.176.34.171:3306/testdb',
  description: '',
  username: '',
  password: '',
});


// 点击按钮处理函数
const handleViewDatabaseTables = async (row) => {
  await getDatabaseTableList(row.uid)
  queryForm.value.applicationId = applicationId
  queryForm.value.databaseId = row.uid
  databaseControlDialogVisible.value = true
}

const handleViewDatabaseTableExample = (row) => {
  exampleTableData.value = parseExampleData(row.example)
  exampleDialogVisible.value = true
}

const handleViewDatabaseTableSchemaExample = (row) => {
  try {
    schemaTableData.value = JSON.parse(row.schemaExample)
    schemaDialogVisible.value = true
  } catch (error) {
    console.error('Failed to parse schema example:', error)
  }
}

const handleViewOpenQueryDialog = (row) => {
  agentId.value = row.agentId
  schemaTableData.value = JSON.parse(row.schemaExample) 
  queryForm.value.tableName = row.name; // 可以从表格中获取实际表名
  queryDialogVisible.value = true;
};

//函数
const getDatabaseList = async () => {
  try{
    const res = await getDatabase(agentId.value)
    if (Array.isArray(res.data.data)) {
      // 对于res.data.data中的每一个进行操作
      databaseData.value = res.data.data
      // 现在可以安全地调用 includes
    } else {
      console.error('Expected an array, but got', res.data)
    }
  }catch (error) {
    console.error('Failed to get database list:', error)
  }
}

const getDatabaseTableList = async(databaseId) => {
  try{
    const res = await getDatabaseTable(databaseId)
    if (Array.isArray(res.data.data)) {
      // 对于res.data.data中的每一个进行操作
      databaseTableData.value = res.data.data
      // 现在可以安全地调用 includes
    } else {
      console.error('Expected an array, but got', res.data)
    }
  }catch(error) {
    console.error('Failed to get database table list:', error)
  }
}

const parseExampleData = (exampleString) => {
// 提取字段名部分和值部分
const [fieldsPart, valuesPart] = exampleString.split('VALUES');

// 提取字段名
const columnNames = fieldsPart.match(/\(([^)]+)\)/)[1].split(',').map(name => name.trim().replace(/`/g, ''));

// 提取值，确保匹配带引号的值、NULL 和其他非引号包裹的内容
const values = [];
const valueRegex = /'(.*?)'|NULL/g;
let match;

while ((match = valueRegex.exec(valuesPart)) !== null) {
  values.push(match[1] !== undefined ? match[1] : 'NULL');
}

// 将字段名与对应的值组合成对象
const parsedData = columnNames.map((name, index) => ({
  column_name: name,
  value: values[index],
}));

return parsedData;
};

// 动态添加条件
const addCondition = () => {
  queryForm.value.conditions.push({
    column: '',
    operator: '=',
    value: '',
  });
};

// 移除条件
const removeCondition = (index) => {
  queryForm.value.conditions.splice(index, 1);
};

// 生成查询语句
const generateQuery = async () => {
  const queryObject = {
    tableName: queryForm.value.tableName,
    columns: queryForm.value.columns,
    conditions: {},
    orderBy: {},
    limit: queryForm.value.limit,
    offset: queryForm.value.offset,
  };

  queryForm.value.conditions.forEach(condition => {
    let value = condition.value;

    // 如果操作符是LIKE，自动添加百分号%
    if (condition.operator === 'LIKE') {
      value = `%${value}%`;
    }

    queryObject.conditions[condition.column] = {
      operator: condition.operator,
      value: value,
    };
  });

  if (queryForm.value.orderBy.column) {
    queryObject.orderBy[queryForm.value.orderBy.column] = queryForm.value.orderBy.order;
  }

  try{
    const res = await query(queryForm.value.applicationId,agentId.value,queryForm.value.databaseId,queryObject)
    //
    // querySuccessMessage.value = res.data.message
    querySuccessMessage.value = "查询成功"  
    querySuccessVisible.value = true
  }catch(error) {
    queryFailedMessage.value = '正在查询，结果文件将保存至结果管理区'
    queryFailedVisible.value = true
  }
  // alert(JSON.stringify(queryObject, null, 2));
  // // 在这里发送查询对象到后端
};

// 新增数据库
const addDatabaseMethod = async () => {
  try{
    const res = await addDatabase(addForm)
    querySuccessMessage.value = "新增成功"
    querySuccessVisible.value = true
  }catch(error) {
    queryFailedMessage.value = 'Failed to add:' + error
    queryFailedVisible.value = true
  }
};

const getAgentMethod = async () => {
  const res = await getAgent()
  agents.value = res.data.body.data.map(item => ({
    value: item.uid,
    label: item.uid
  }))
}

const handleSelectAgent = async (value) => {
  agentId.value = value
  getDatabaseList()
}

</script>
<style scoped></style>

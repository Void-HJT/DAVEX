<template>
  <el-container>
    <el-header style="height: 50px">
      <div
        style="
          background-color: antiquewhite;
          height: 40px;
          display: flex;
          justify-content: center;
          align-items: center;
        "
      >
        <p
          style="
            font-size: 20px;
            color: black;
            opacity: 100%;
            text-align: center;
          "
        >
          接入数据库表
        </p>
      </div>
    </el-header>
      <el-table
        :data="databaseData"
        style="width: 100%"
        stripe
        height="200"
        max-height="200"
      >
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
          mid-width="60"
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
            </div>
          </template>

        </el-table-column>
      </el-table>
  </el-container>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'

import {
  getDatabase,
} from '../../api/query.js'

//初始化
onMounted(() => {
  getDatabaseList()
})

// 表格数据
const databaseData = ref([])

const getDatabaseList = async () => {
  try{
    const res = await getDatabase()
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
</script>



<style scoped></style>

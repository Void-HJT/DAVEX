module.exports = {
  //运行环境
  env: {
    browser: true, //浏览器端
    es2021: true, //es2021
    node: true,
    jest: true,
  },
  //规则继承
  extends: [
    //全部规则默认是关闭的,这个配置项开启推荐规则,推荐规则参照文档
    //比如:函数不能重名、对象不能出现重复key
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended', //ts语法规则
    'plugin:vue/vue3-essential', //vue3语法规则
    'plugin:prettier/recommended',
  ],
  //要为特定类型的文件指定处理器
  overrides: [
    {
      env: {
        node: true,
      },
      files: ['.eslintrc.{js,cjs}'],
      parserOptions: {
        sourceType: 'script',
      },
    },
  ],

  /* 指定如何解析语法 */
  parser: 'vue-eslint-parser',
  /** 优先级低于 parse 的语法解析配置 */
  /**
   * 指定解析器:解析器
   * Esprima 默认解析器Babel-ESLint babel解析器
   * @typescript-eslint/parser ts解析器
   */
  parserOptions: {
    ecmaVersion: 'latest', //校验ECMA最新版本
    parser: '@typescript-eslint/parser',
    sourceType: 'module', //设置为"script"（默认），或者"module"代码在ECMAScript模块中
    jsxPragma: 'React',
    ecmaFeatures: {
      jsx: true,
    },
  },
  /**
   * ESLint支持使用第三方插件。在使用插件之前，您必须使用npm安装它
   * 该eslint-plugin-前缀可以从插件名称被省略
   */
  plugins: ['@typescript-eslint', 'vue'],

  /*
   * eslint规则
   * "off" 或 0    ==>  关闭规则
   * "warn" 或 1   ==>  打开的规则作为警告（不影响代码执行）
   * "error" 或 2  ==>  规则作为一个错误（代码不能执行，界面报错）
   */
  rules: {
    // eslint（https://zh-hans.eslint.org/docs/latest/rules/）
    'no-var': 'error', // 要求使用 let 或 const 而不是 var
    'no-multiple-empty-lines': ['warn', { max: 1 }], // 不允许多个空行
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-unexpected-multiline': 'error', // 禁止空余的多行
    'space-before-function-paren': 'off', // 函数括号前面是否需要空格
    'no-use-before-define': 'off', // 禁止在变量定义前使用变量
    'no-unused-vars': 'off', // 禁止未使用过的变量
    'no-undef': 'off', // 禁止使用未定义的变量
    'no-useless-escape': 'off', // 禁止不必要的转义字符
    'prettier/prettier': 'error', // 代码格式化
    'comma-dangle': 'off', // 对象或数组最后一个元素后面是否需要加逗号
    // 结尾必须有分号;
    semi: [
      'error',
      'always',
      {
        omitLastInOneLineBlock: true,
      },
    ],

    // typeScript (https://typescript-eslint.io/rules)
    '@typescript-eslint/no-unused-vars': 'error', // 禁止定义未使用的变量
    '@typescript-eslint/prefer-ts-expect-error': 'off', // 禁止使用 @ts-ignore
    '@typescript-eslint/no-explicit-any': 'off', // 禁止使用 any 类型
    '@typescript-eslint/no-var-requires': 'off', // 允许使用 require() 函数导入模块
    '@typescript-eslint/no-empty-function': 'off', // 禁止空函数
    '@typescript-eslint/no-use-before-define': 'off', // 禁止在 函数/类/变量 定义之前使用它们
    '@typescript-eslint/ban-types': 'off', // 禁止使用特定类型
    '@typescript-eslint/no-non-null-assertion': 'off', // 不允许使用后缀运算符的非空断言(!)
    '@typescript-eslint/no-namespace': 'off', // 禁止使用自定义 TypeScript 模块和命名空间。
    '@typescript-eslint/explicit-module-boundary-types': 'off', // 要求函数和类方法的显式返回类型
    '@typescript-eslint/ban-ts-comment': 'error', // 禁止在类型注释或类型断言中使用 // @ts-ignore
    '@typescript-eslint/semi': 'off',

    // eslint-plugin-vue (https://eslint.vuejs.org/rules/)
    'vue/multi-word-component-names': 'off', // 要求组件名称始终为 “-” 链接的单词
    'vue/no-v-model-argument': 'off', // 禁止在 v-model 指令中使用 argument 选项
    'vue/no-reserved-component-names': 'off', // 禁止使用保留字命名组件
    'vue/attributes-order': 'off', // 禁止组件的属性顺序不一致
    'vue/one-component-per-file': 'off', // 要求每个文件只有一个组件
    'vue/no-multiple-template-root': 'off', // 禁止在单个文件中使用多个根元素
    'vue/max-attributes-per-line': 'off', // 限制每行属性的最大数量
    'vue/multiline-html-element-content-newline': 'off', // 限制多行 HTML 元素内容的缩进
    'vue/singleline-html-element-content-newline': 'off', // 限制单行 HTML 元素内容的缩进
    'vue/require-default-prop': 'off', // 禁止在 props 定义中不指定默认值
    'vue/require-explicit-emits': 'off', // 要求显式声明 emits 事件
    'vue/html-closing-bracket-newline': 'off', // 禁止在 HTML 结束标签的前后都有换行符
    'vue/attribute-hyphenation': 'off', // 强制属性命名使用连字符线分隔
    'vue/script-setup-uses-vars': 'error', // 防止<script setup>使用的变量<template>被标记为未使用
    'vue/no-mutating-props': 'off', // 不允许组件 prop的改变
    'vue/no-v-html': 'off', // 禁止使用v-html指令
    'vue/custom-event-name-casing': 'error', // 自定义事件名称必须使用驼峰式命名法
  },
};


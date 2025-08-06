<template>
  <div class="advanced-search-form">
    <a-row :gutter="16">
      <a-col v-for="field in fields" :key="field.key" :span="field.span || 6">
        <!-- 输入框 -->
        <a-input
          v-if="field.type === 'input'"
          v-model:value="formData[field.key]"
          :placeholder="field.placeholder"
          @change="handleFieldChange"
          :allow-clear="field.allowClear !== false"
        />
        
        <!-- 选择框 -->
        <a-select
          v-else-if="field.type === 'select'"
          v-model:value="formData[field.key]"
          :placeholder="field.placeholder"
          :allow-clear="field.allowClear !== false"
          @change="handleFieldChange"
        >
          <a-select-option 
            v-for="option in field.options" 
            :key="option.value" 
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
        
        <!-- 日期选择器 -->
        <a-date-picker
          v-else-if="field.type === 'date'"
          v-model:value="formData[field.key]"
          :placeholder="field.placeholder"
          @change="handleFieldChange"
          style="width: 100%"
        />
        
        <!-- 日期范围选择器 -->
        <a-range-picker
          v-else-if="field.type === 'dateRange'"
          v-model:value="formData[field.key]"
          @change="handleFieldChange"
          style="width: 100%"
        />
      </a-col>
      
      <!-- 操作按钮 -->
      <a-col :span="actionSpan">
        <a-space>
          <a-button type="primary" @click="handleSearch" :loading="loading">
            <SearchOutlined />
            搜索
          </a-button>
          <a-button @click="handleReset">
            重置
          </a-button>
          <slot name="extra-actions"></slot>
        </a-space>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'

export interface SearchField {
  key: string
  type: 'input' | 'select' | 'date' | 'dateRange'
  placeholder: string
  span?: number
  allowClear?: boolean
  options?: Array<{ label: string; value: any }>
}

interface Props {
  fields: SearchField[]
  modelValue?: Record<string, any>
  loading?: boolean
  actionSpan?: number
  autoSearch?: boolean
  debounceTime?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({}),
  loading: false,
  actionSpan: 6,
  autoSearch: false,
  debounceTime: 300
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
  search: [value: Record<string, any>]
  reset: []
}>()

// 表单数据
const formData = reactive<Record<string, any>>({})

// 初始化表单数据
const initFormData = () => {
  props.fields.forEach(field => {
    formData[field.key] = props.modelValue[field.key] || (field.type === 'dateRange' ? [] : undefined)
  })
}

// 防抖定时器
let debounceTimer: NodeJS.Timeout | null = null

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  Object.assign(formData, newValue)
}, { immediate: true, deep: true })

// 监听表单数据变化
watch(formData, (newValue) => {
  emit('update:modelValue', { ...newValue })
}, { deep: true })

// 字段变化处理
const handleFieldChange = () => {
  if (props.autoSearch) {
    // 清除之前的定时器
    if (debounceTimer) {
      clearTimeout(debounceTimer)
    }
    
    // 设置新的定时器
    debounceTimer = setTimeout(() => {
      handleSearch()
    }, props.debounceTime)
  }
}

// 搜索处理
const handleSearch = () => {
  // 过滤空值
  const searchData = Object.keys(formData).reduce((acc, key) => {
    const value = formData[key]
    if (value !== undefined && value !== null && value !== '' && 
        !(Array.isArray(value) && value.length === 0)) {
      acc[key] = value
    }
    return acc
  }, {} as Record<string, any>)
  
  emit('search', searchData)
}

// 重置处理
const handleReset = () => {
  props.fields.forEach(field => {
    formData[field.key] = field.type === 'dateRange' ? [] : undefined
  })
  emit('reset')
  if (props.autoSearch) {
    handleSearch()
  }
}

// 初始化
initFormData()
</script>

<style scoped>
.advanced-search-form {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.advanced-search-form :deep(.ant-col) {
  margin-bottom: 16px;
}

.advanced-search-form :deep(.ant-col:last-child) {
  margin-bottom: 0;
}
</style>
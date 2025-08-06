/**
 * 代码生成类型常量
 * 对应后端 CodeGenTypeEnum 枚举
 */

export interface CodeGenType {
  label: string
  value: string
}

/**
 * 代码生成类型枚举
 */
export const CODE_GEN_TYPES: CodeGenType[] = [
  {
    label: '原生 HTML 模式',
    value: 'html'
  },
  {
    label: '原生多文件模式', 
    value: 'multi_file'
  }
]

/**
 * 根据值获取标签
 */
export const getCodeGenTypeLabel = (value: string): string => {
  const type = CODE_GEN_TYPES.find(item => item.value === value)
  return type?.label || value
}

/**
 * 根据标签获取值
 */
export const getCodeGenTypeValue = (label: string): string => {
  const type = CODE_GEN_TYPES.find(item => item.label === label)
  return type?.value || label
}

/**
 * 代码生成类型映射对象
 */
export const CODE_GEN_TYPE_MAP = CODE_GEN_TYPES.reduce((map, type) => {
  map[type.value] = type.label
  return map
}, {} as Record<string, string>)
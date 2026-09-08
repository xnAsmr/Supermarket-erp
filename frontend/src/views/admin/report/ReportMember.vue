<template>
  <div class="report-member">
    <n-space vertical :size="16">
      <n-grid :cols="4" :x-gap="16">
        <n-gi>
          <n-card>
            <n-statistic label="总会员数" :value="memberData.totalMembers || 0">
              <template #suffix>
                <n-text depth="3" style="font-size: 14px">人</n-text>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card>
            <n-statistic label="本月新增" :value="memberData.newMembersThisMonth || 0">
              <template #suffix>
                <n-text depth="3" style="font-size: 14px">人</n-text>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card>
            <n-statistic label="活跃会员" :value="memberData.activeMembers || 0">
              <template #suffix>
                <n-text depth="3" style="font-size: 14px">人</n-text>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card>
            <n-statistic label="储值总额" :value="memberData.totalBalance || 0" :precision="2">
              <template #prefix>¥</template>
            </n-statistic>
          </n-card>
        </n-gi>
      </n-grid>

      <n-card title="会员消费统计" :bordered="false">
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-card size="small">
              <n-statistic label="累计消费总额" :value="memberData.totalConsume || 0" :precision="2">
                <template #prefix>¥</template>
              </n-statistic>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card size="small">
              <n-statistic label="人均消费" :value="avgConsume" :precision="2">
                <template #prefix>¥</template>
              </n-statistic>
            </n-card>
          </n-gi>
        </n-grid>
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NCard, NSpace, NStatistic, NGrid, NGi, NText } from 'naive-ui'
import { getMemberReport } from '../../../api/report'
import type { MemberReportVO } from '../../../api/report'

const memberData = ref<MemberReportVO>({
  totalMembers: 0,
  newMembersThisMonth: 0,
  activeMembers: 0,
  totalBalance: 0,
  totalConsume: 0
})

const avgConsume = computed(() => {
  const total = memberData.value.totalMembers || 0
  const consume = memberData.value.totalConsume || 0
  return total > 0 ? consume / total : 0
})

async function loadData() {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  try {
    memberData.value = await getMemberReport(year, month)
  } catch (e) {
    console.error('加载会员报表失败', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.report-member { padding: 0; }
</style>

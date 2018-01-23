
package com.zero.rpc.consumer.loadbalance;


import com.zero.transport.Directory;
import com.zero.transport.api.channel.CopyOnWriteGroupList;
import com.zero.transport.api.channel.ChannelGroup;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机负载均衡.
 *
 * <pre>
 * *****************************************************************************
 *
 *            random value
 * ─────────────────────────────────▶
 *                                  │
 *                                  ▼
 * ┌─────────────────┬─────────┬──────────────────────┬─────┬─────────────────┐
 * │element_0        │element_1│element_2             │...  │element_n        │
 * └─────────────────┴─────────┴──────────────────────┴─────┴─────────────────┘
 *
 * *****************************************************************************
 * </pre>
 *
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    private static final RandomLoadBalancer instance = new RandomLoadBalancer();

    public static RandomLoadBalancer instance() {
        return instance;
    }

    @Override
    public ChannelGroup select(CopyOnWriteGroupList groups, Directory directory) {
        ChannelGroup[] elements = groups.snapshot();
        int length = elements.length;

        if (length == 0) {
            return null;
        }

        if (length == 1) {
            return elements[0];
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        if (groups.isSameWeight()) {
            // 对于大多数场景, 在预热都完成后, 很可能权重都是相同的, 那么加权随机算法将是没有必要的开销,
            // 如果发现一个CopyOnWriteGroupList里面所有元素权重相同, 会设置一个sameWeight标记,
            // 下一次直接退化到普通随机算法, 如果CopyOnWriteGroupList中元素出现变化, 标记会被自动取消.
            return elements[random.nextInt(length)];
        }

        // 遍历权重
        boolean allWarmUpComplete = true;
        int sumWeight = 0;
        WeightArray weightsSnapshot = weightArray(length);
        for (int i = 0; i < length; i++) {
            ChannelGroup group = elements[i];

            int val = getWeight(group, directory);

            weightsSnapshot.set(i, val);
            sumWeight += val;
            allWarmUpComplete = (allWarmUpComplete && group.isWarmUpComplete());
        }

        boolean sameWeight = true;
        int val_0 = weightsSnapshot.get(0);
        for (int i = 1; i < length && sameWeight; i++) {
            sameWeight = (val_0 == weightsSnapshot.get(i));
        }

        if (allWarmUpComplete && sameWeight) {
            // 预热全部完成并且权重完全相同
            groups.setSameWeight(true);
        }

        // 这一段算法参考当前的类注释中的那张图
        //
        // 如果权重不相同且总权重大于0, 则按总权重数随机
        if (!sameWeight && sumWeight > 0) {
            int offset = random.nextInt(sumWeight);
            // 确定随机值落在哪个片
            for (int i = 0; i < length; i++) {
                offset -= weightsSnapshot.get(i);
                if (offset < 0) {
                    return elements[i];
                }
            }
        }

        return elements[random.nextInt(length)];
    }
}

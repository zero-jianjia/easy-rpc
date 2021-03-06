package com.zero.rpc.consumer.loadbalance;


import com.zero.transport.Directory;
import com.zero.transport.api.channel.ChannelGroup;
import com.zero.transport.api.channel.CopyOnWriteGroupList;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 加权轮询负载均衡.
 * <p>
 * 当前实现不会先去计算最大公约数再轮询, 通常最大权重和最小权重值不会相差过于悬殊,
 * 因此我觉得没有必要先去求最大公约数, 很可能产生没有必要的开销.
 * <p>
 * 每个服务应有各自独立的实例(index不共享)
 * <p>
 * <pre>
 * **********************************************************************
 *
 *  index++ % sumWeight
 *
 *                       ┌─┐
 *                       │ │
 *                       │ │                 ┌─┐
 *             ┌─┐       │ │                 │ │
 *             │ │       │ │                 │ │
 *             │ │       │ │                 │ │
 *             │ │       │ │  ┌─┐       ┌─┐  │ │
 * ════════════╬═╬═══════╬═╬══╬═╬═══════╬═╬▶ │ │
 *        ┌─┐  │ │       │ │  │ │  ┌─┐  │ │  │ │
 *        │ │  │ │       │ │  │ │  │ │  │ │  │ │
 * ═══════╬═╬══╬═╬═══════╬═╬══╬═╬══╬═╬══╬═╬══╬═╬══▶
 *        │ │  │ │       │ │  │ │  │ │  │ │  │ │
 *        │ │  │ │  ┌─┐  │ │  │ │  │ │  │ │  │ │
 * ═══════╬═╬══╬═╬══╬═╬══╬═╬══╬═╬══╬═╬══╬═╬══╬═╬══▶
 *        │ │  │ │  │ │  │ │  │ │  │ │  │.│  │ │
 *        │ │  │ │  │ │  │ │  │ │  │ │  │.│  │ │
 *        │0│  │1│  │2│  │3│  │4│  │5│  │.│  │n│
 *        └─┘  └─┘  └─┘  └─┘  └─┘  └─┘  └─┘  └─┘
 *
 * **********************************************************************
 * </pre>
 * @author jiachun.fjc
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    private static final AtomicIntegerFieldUpdater<RoundRobinLoadBalancer> indexUpdater =
            AtomicIntegerFieldUpdater.newUpdater(RoundRobinLoadBalancer.class, "index");

    @SuppressWarnings("unused")
    private volatile int index = 0;

    public static RoundRobinLoadBalancer instance() {
        // round-robin是有状态(index)的, 不能是单例
        return new RoundRobinLoadBalancer();
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

        int index = indexUpdater.getAndIncrement(this) & Integer.MAX_VALUE;

        if (groups.isSameWeight()) {
            // 对于大多数场景, 在预热都完成后, 很可能权重都是相同的, 那么加权轮询算法将是没有必要的开销,
            // 如果发现一个CopyOnWriteGroupList里面所有元素权重相同, 会设置一个sameWeight标记,
            // 下一次直接退化到普通随机算法, 如果CopyOnWriteGroupList中元素出现变化, 标记会被自动取消.
            return elements[index % length];
        }

        // 遍历权重
        int sumWeight = 0;
        WeightArray weightsSnapshot = weightArray(length);
        for (int i = 0; i < length; i++) {
            ChannelGroup group = elements[i];

            int val = getWeight(group, directory);

            weightsSnapshot.set(i, val);
            sumWeight += val;
        }

        int maxWeight = 0;
        int minWeight = Integer.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            int val = weightsSnapshot.get(i);
            maxWeight = Math.max(maxWeight, val);
            minWeight = Math.min(minWeight, val);
        }

        if (maxWeight > 0 && minWeight == maxWeight) {
            groups.setSameWeight(true);
        }

        // 这一段算法参考当前的类注释中的那张图
        //
        // 当前实现不会先去计算最大公约数再轮询, 通常最大权重和最小权重值不会相差过于悬殊,
        // 因此我觉得没有必要先去求最大公约数, 很可能产生没有必要的开销.
        if (maxWeight > 0 && minWeight < maxWeight) {
            int mod = index % sumWeight;
            for (int i = 0; i < maxWeight; i++) {
                for (int j = 0; j < length; j++) {
                    int val = weightsSnapshot.get(j);
                    if (mod == 0 && val > 0) {
                        return elements[j];
                    }
                    if (val > 0) {
                        weightsSnapshot.set(j, val - 1);
                        --mod;
                    }
                }
            }
        }

        return elements[index % length];
    }
}

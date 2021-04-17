package org.seckill.service.mock.strategy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.seckill.api.constant.SeckillStatusConstant;
import org.seckill.api.dto.SeckillMockRequestDto;
import org.seckill.entity.Seckill;
import org.seckill.mp.dao.mapper.SeckillMapper;
import org.seckill.mp.dao.mapper.SuccessKilledMapper;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedLockStrategyTest {
    @InjectMocks
    private SynchronizedLockStrategy synchronizedLockStrategy;
    @Mock
    private SeckillMapper seckillMapper;
    @Mock
    private SuccessKilledMapper successKilledMapper;
    @Spy
    private ThreadPoolExecutor taskExecutor =
            new ThreadPoolExecutor(1,1,2L,
                    TimeUnit.SECONDS, new ArrayBlockingQueue(1));
    @Mock
    private Source source;
    @Mock
    private MessageChannel messageChannel;

    @Test
    public void execute() {
        SeckillMockRequestDto requestDto = new SeckillMockRequestDto();
        long seckillId = 1L;
        requestDto.setSeckillId(seckillId);
        requestDto.setCount(2);
        requestDto.setPhoneNumber("1");

        Seckill sendTopicResult = new Seckill();
        sendTopicResult.setSeckillId(seckillId);
        sendTopicResult.setStatus(SeckillStatusConstant.END);

        Seckill seckill1 = new Seckill();
        seckill1.setNumber(0);
        when(seckillMapper.selectById(seckillId)).thenReturn(seckill1);
        when(source.output()).thenReturn(messageChannel);
        synchronizedLockStrategy.execute(requestDto);
        verify(seckillMapper, times(2)).updateById(sendTopicResult);
    }

    @Test
    public void executeNumberGt0() {
        SeckillMockRequestDto requestDto = new SeckillMockRequestDto();
        long seckillId = 1L;
        requestDto.setSeckillId(seckillId);
        requestDto.setCount(1);
        requestDto.setPhoneNumber("1");

        Seckill seckill = new Seckill();
        seckill.setNumber(1);
        when(seckillMapper.selectById(seckillId)).thenReturn(seckill);
        synchronizedLockStrategy.execute(requestDto);

        Seckill sendTopicResult = new Seckill();
        sendTopicResult.setSeckillId(seckillId);
        sendTopicResult.setStatus(SeckillStatusConstant.END);
        verify(seckillMapper, never()).updateById(sendTopicResult);
    }
}
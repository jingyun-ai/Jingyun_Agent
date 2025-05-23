package com.cn.bdth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.bdth.dto.StarDialogueDto;
import com.cn.bdth.dto.StarPresetDto;
import com.cn.bdth.entity.Star;
import com.cn.bdth.entity.StarPreset;
import com.cn.bdth.mapper.StarMapper;
import com.cn.bdth.mapper.StarPresetMapper;
import com.cn.bdth.service.StarService;
import com.cn.bdth.utils.BeanUtils;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.vo.UserStarDetailVo;
import com.cn.bdth.vo.UserStarVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StarServiceImpl implements StarService {

    private final StarMapper starMapper;
    private final StarPresetMapper starPresetMapper;

    @Override
    public long starDialogue(final StarDialogueDto dto) {
        final Star star = new Star()
                .setUserId(UserUtils.getCurrentLoginId())
                .setIssue(dto.getIssue())
                .setAnswer(dto.getAnswer());
        starMapper.insert(star);

        return star.getStarId();
    }

    @Override
    public String starPreset(final StarPresetDto dto) {
        StarPreset find = starPresetMapper.selectOne(new QueryWrapper<StarPreset>()
                .lambda().eq(StarPreset::getStarId, UserUtils.getCurrentLoginId()+dto.getTitle())
                .select(StarPreset::getTitle, StarPreset::getCreatedTime, StarPreset::getStarId)
                .orderByDesc(StarPreset::getCreatedTime)
        );

        if (find == null) {
            final StarPreset starPreset = new StarPreset()
                    .setStarId(UserUtils.getCurrentLoginId()+dto.getTitle())
                    .setUserId(UserUtils.getCurrentLoginId())
                    .setTitle(dto.getTitle());
            starPresetMapper.insert(starPreset);
            return starPreset.getStarId();
        } else{// 删除
            starPresetMapper.delete(new QueryWrapper<StarPreset>()
                    .lambda()
                    .eq(StarPreset::getStarId, UserUtils.getCurrentLoginId()+dto.getTitle())
                    .eq(StarPreset::getUserId, UserUtils.getCurrentLoginId())
            );
            return "Delete";
        }
    }

    @Override
    public IPage<UserStarVo> getUserStarVoPage(final int pageNum) {
        final Long currentLoginId = UserUtils.getCurrentLoginId();
        return starMapper.selectPage(new Page<>(pageNum, 15), new QueryWrapper<Star>()
                .lambda()
                .eq(Star::getUserId, currentLoginId)
                .select(Star::getIssue, Star::getCreatedTime, Star::getStarId)
        ).convert(c -> new UserStarVo().setIssue(c.getIssue()).setStarId(c.getStarId()).setCreatedTime(c.getCreatedTime()));
    }

    @Override
    public UserStarDetailVo getUserStarDetail(final Long id) {

        final Star star = starMapper.selectOne(new QueryWrapper<Star>()
                .lambda().eq(Star::getStarId, id)
                .eq(Star::getUserId, UserUtils.getCurrentLoginId())
                .select(Star::getAnswer, Star::getIssue, Star::getStarId, Star::getCreatedTime)
        );
        if (star == null) {
            return null;
        }
        return BeanUtils.copyClassProperTies(star, UserStarDetailVo.class);

    }

    @Override
    public void deleteById(final Long id) {
        starMapper.delete(new QueryWrapper<Star>()
                .lambda()
                .eq(Star::getStarId, id)
                .eq(Star::getUserId, UserUtils.getCurrentLoginId())
        );
    }

    @Override
    public List<UserStarVo> getUserStarWeb() {
        return starMapper.selectList(new QueryWrapper<Star>()
                .lambda().eq(Star::getUserId, UserUtils.getCurrentLoginId())
                .select(Star::getAnswer, Star::getIssue, Star::getCreatedTime, Star::getStarId)
                .orderByDesc(Star::getCreatedTime)
        ).stream().map(s -> new UserStarVo().setStarId(s.getStarId()).setAnswer(s.getAnswer()).setIssue(s.getIssue()).setCreatedTime(s.getCreatedTime())).toList();
    }

    @Override
    public List<String> getUserStarPresetWeb() {
        return starPresetMapper.selectList(new QueryWrapper<StarPreset>()
                .lambda().eq(StarPreset::getUserId, UserUtils.getCurrentLoginId())
                .select(StarPreset::getTitle, StarPreset::getCreatedTime, StarPreset::getStarId)
                .orderByDesc(StarPreset::getCreatedTime)
        ).stream().map(s -> s.getTitle()).collect(Collectors.toList());
    }
}

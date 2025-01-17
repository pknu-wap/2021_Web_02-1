package com.web02.web;
import com.web02.domain.posts.Posts;
import com.web02.domain.posts.PostsRepository;
import com.web02.web.dto.PostsUpdateRequestDto;
import com.web02.web.dto.PostsSaveRequestDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// For mockMvc

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private PostsRepository postsRepository;

        @Autowired
        private WebApplicationContext context;

        private MockMvc mvc;

        @Before
        public void setup() {
                mvc = MockMvcBuilders
                        .webAppContextSetup(context)
                        .build();
        }

        @After
        public void tearDown() throws Exception {
                postsRepository.deleteAll();
        }

        @Test
        @WithMockUser(roles="USER")//USER 권한부여
        public void 게시물등록() throws Exception {
                //given
                String title = "title";
                String content = "content";
                String author="author";
                PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                        .title(title)
                        .content(content)
                        .author(author)
                        .build();

                String url = "http://localhost:" + port + "/api/v1/posts";

                //when
                mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                        .andExpect(status().isOk());

                //then
                List<Posts> all = postsRepository.findAll();
                assertThat(all.get(0).getTitle()).isEqualTo(title);
                assertThat(all.get(0).getContent()).isEqualTo(content);
        }

        @Test
        @WithMockUser(roles="USER")
        public void 게시물수정() throws Exception {
                //given
                Posts savedPosts = postsRepository.save(Posts.builder()
                        .title("title")
                        .content("content")
                        .author("author")
                        .build());

                Long updateId = savedPosts.getId();
                String expectedTitle = "title2";
                String expectedContent = "content2";

                PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder()
                        .title(expectedTitle)
                        .content(expectedContent)
                        .build();

                String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

                //when
                mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                        .andExpect(status().isOk());

                //then
                List<Posts> all = postsRepository.findAll();
                assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
                assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
        }
}
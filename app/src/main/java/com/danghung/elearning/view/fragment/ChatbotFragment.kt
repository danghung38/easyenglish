package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.databinding.FragmentChatbotBinding
import com.danghung.elearning.model.ChatMessage
import com.danghung.elearning.view.adapter.ChatAdapter
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_HOME
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_PROFILE
import com.danghung.elearning.viewmodel.ChatbotVM

class ChatbotFragment : BaseFragment<FragmentChatbotBinding, ChatbotVM>() {

    companion object {
        val TAG: String = ChatbotFragment::class.java.name
    }

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    override fun getClassVM(): Class<ChatbotVM> = ChatbotVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentChatbotBinding = FragmentChatbotBinding.inflate(inflater, container, false)

    override fun initViews() {
        initEdtSearch()
        setupRecyclerView()
        setupClickListeners()

        // Vì không lưu trạng thái chat, luôn thêm câu chào mặc định mỗi lần mở Fragment
        addWelcomeMessage()
        viewModel.getMyInfo()
        setUpOnBackMenu(TYPE_HOME)
    }


    private fun initEdtSearch() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, 0, 0, maxOf(imeInsets.bottom, navInsets.bottom))
            insets
        }
    }

    override fun clickView(v: View) {
        // Xử lý click tập trung nếu cần
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(
            onAnimationEnd = {
                binding.rcvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
            },
            avatarUrl = viewModel.avatarUrl  // ← thêm dòng này
        )

        binding.rcvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_HOME, false)
        }

        binding.btnSend.setOnClickListener {
            performSendMessage()
        }
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            message = "Hi! I am KMA English Assistant. How can I help you practice your English today? 🤖",
            isUser = false,
            isAnimating = true // Cho chạy chữ ngay tại câu chào đầu tiên
        )
        messageList.add(welcomeMessage)
        chatAdapter.submitList(messageList)
    }

    private fun performSendMessage() {
        val userText = binding.edtMessage.text.toString().trim()
        if (userText.isEmpty()) return


        binding.btnSend.isEnabled = false
        binding.btnSend.alpha = 0.4f  // ← xám đi

        // 1. Hiển thị tin nhắn user
        val userMessage = ChatMessage(message = userText, isUser = true)
        chatAdapter.addMessage(userMessage)
        binding.edtMessage.setText("")

        // 2. Thêm typing bubble ngay lập tức
        val typingMessage = ChatMessage(message = "", isUser = false, isTyping = true)
        chatAdapter.addMessage(typingMessage)
        binding.rcvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)

        // 3. Gọi API
        viewModel.sendChat(userText)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            ChatbotVM.SEND_CHAT -> {
                binding.btnSend.isEnabled = true
                binding.btnSend.alpha = 1.0f  // ← về màu bình thường
                val botReplyText = data as? String ?: "Hệ thống bận, vui lòng thử lại sau."
                val botMessage = ChatMessage(
                    message = botReplyText,
                    isUser = false,
                    isAnimating = true
                )
                // Xóa typing bubble, thêm reply thật
                chatAdapter.removeTypingAndAddReply(botMessage)
                binding.rcvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
            ChatbotVM.GET_MY_INFO -> {
                chatAdapter.updateAvatarUrl(viewModel.avatarUrl)
            }
        }
    }

    override fun handleApiError(key: String, code: Int, error: ApiErrorRes?) {
        binding.btnSend.isEnabled = true
        binding.btnSend.alpha = 1.0f  // ← về màu bình thường khi lỗi
        super.handleApiError(key, code, error)
    }
}
package com.expensetracker.backend.dto;

import java.util.List;

public class GeminiDto {

    // Request DTOs
    public static class GeminiRequest {
        private List<Content> contents;
        private SystemInstruction systemInstruction;

        public GeminiRequest() {
        }

        public GeminiRequest(List<Content> contents, SystemInstruction systemInstruction) {
            this.contents = contents;
            this.systemInstruction = systemInstruction;
        }

        public List<Content> getContents() {
            return contents;
        }

        public void setContents(List<Content> contents) {
            this.contents = contents;
        }

        public SystemInstruction getSystemInstruction() {
            return systemInstruction;
        }

        public void setSystemInstruction(SystemInstruction systemInstruction) {
            this.systemInstruction = systemInstruction;
        }
    }

    public static class Content {
        private List<Part> parts;

        public Content() {
        }

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        private String text;

        public Part() {
        }

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class SystemInstruction {
        private List<Part> parts;

        public SystemInstruction() {
        }

        public SystemInstruction(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    // Response DTOs
    public static class GeminiResponse {
        private List<Candidate> candidates;

        public GeminiResponse() {
        }

        public List<Candidate> getCandidates() {
            return candidates;
        }

        public void setCandidates(List<Candidate> candidates) {
            this.candidates = candidates;
        }
    }

    public static class Candidate {
        private Content content;

        public Candidate() {
        }

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }
    }
}

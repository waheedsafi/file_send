package Zkteco.zkteco.modules.job.service;

import Zkteco.zkteco.domain.common.AsyncJob;
import Zkteco.zkteco.modules.job.dto.AsyncJobResponse;
import Zkteco.zkteco.repository.common.AsyncJobRepository;
import Zkteco.zkteco.web.error.NotFoundException;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsyncJobService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final AsyncJobRepository asyncJobRepository;

    public AsyncJobService(AsyncJobRepository asyncJobRepository) {
        this.asyncJobRepository = asyncJobRepository;
    }

    public AsyncJobResponse create(String action, String payload) {
        AsyncJob job = new AsyncJob();
        job.setAction(action);
        job.setStatus(STATUS_PENDING);
        job.setPayload(payload);
        job.setCreatedAt(OffsetDateTime.now());
        return toResponse(asyncJobRepository.save(job));
    }

    public void markRunning(Long jobId) {
        AsyncJob job = getEntity(jobId);
        job.setStatus(STATUS_RUNNING);
        if (job.getStartedAt() == null) {
            job.setStartedAt(OffsetDateTime.now());
        }
        asyncJobRepository.save(job);
    }

    public void markSuccess(Long jobId, String result) {
        AsyncJob job = getEntity(jobId);
        job.setStatus(STATUS_SUCCESS);
        job.setResult(result);
        job.setFinishedAt(OffsetDateTime.now());
        asyncJobRepository.save(job);
    }

    public void markFailed(Long jobId, String errorMessage) {
        AsyncJob job = getEntity(jobId);
        job.setStatus(STATUS_FAILED);
        job.setErrorMessage(errorMessage);
        job.setFinishedAt(OffsetDateTime.now());
        asyncJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public AsyncJobResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    private AsyncJob getEntity(Long id) {
        return asyncJobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Async job not found: " + id));
    }

    private AsyncJobResponse toResponse(AsyncJob job) {
        return new AsyncJobResponse(
                job.getId(),
                job.getAction(),
                job.getStatus(),
                job.getPayload(),
                job.getResult(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getErrorMessage()
        );
    }
}

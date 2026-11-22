import { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Select,
  MenuItem,
  Chip,
  IconButton,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Typography,
  Card,
  CardContent,
  Grid,
  Snackbar,
  Switch,
  FormControlLabel,
  Slider,
  Divider
} from '@mui/material';
import { Edit, Delete, Add, Refresh, Visibility, DeleteOutline as DeleteIcon, AddCircleOutline as AddIcon } from '@mui/icons-material';
import PropTypes from 'prop-types';
import useAxios from '../../utils/useAxios';

export default function RAGExamplesManagement() {
  const axios = useAxios(); // Use authenticated axios
  const [examples, setExamples] = useState([]);
  const [stats, setStats] = useState(null);
  const [filterType, setFilterType] = useState('all');
  const [filterBand, setFilterBand] = useState('all');
  const [filterTask, setFilterTask] = useState('all');
  const [openDialog, setOpenDialog] = useState(false);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [currentExample, setCurrentExample] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [advancedMode, setAdvancedMode] = useState(false);

  const showMessage = (type, text) => {
    setMessage({ type, text });
  };

  const fetchExamples = useCallback(async () => {
    try {
      setLoading(true);
      const params = {};
      if (filterType !== 'all') params.type = filterType;
      if (filterBand !== 'all') params.band = parseFloat(filterBand);
      if (filterTask !== 'all') params.task = filterTask;

      const response = await axios.get('/admin/rag-examples', { params });
      setExamples(response.data.result || []);
    } catch (error) {
      console.error('Error loading examples:', error);
      showMessage('error', 'Failed to load examples');
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterType, filterBand, filterTask]);

  const fetchStats = useCallback(async () => {
    try {
      const response = await axios.get('/admin/rag-examples/stats');
      setStats(response.data.result);
    } catch (error) {
      console.error('Error loading stats:', error);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    fetchExamples();
    fetchStats();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterType, filterBand, filterTask]);

  const handleCreate = () => {
    setCurrentExample({
      text: '',
      evaluation: {
        overallBand: 5.0,
        criteriaScores: {
          fluency_coherence: 5.0,
          lexical_resource: 5.0,
          grammatical_range_accuracy: 5.0,
          pronunciation: 5.0
        },
        detailedFeedback: '',
        examinerFeedback: '',
        strengths: [],
        areasForImprovement: []
      },
      metadata: {
        type: 'speaking',
        band: 5.0,
        task: 'task2'
      }
    });
    setAdvancedMode(false); // Reset to quick mode
    setOpenDialog(true);
  };

  const handleEdit = (example) => {
    // Convert from snake_case to camelCase
    const converted = {
      ...example,
      evaluation: {
        overallBand: example.evaluation.overall_band,
        criteriaScores: example.evaluation.criteria_scores,
        detailedFeedback: example.evaluation.detailed_feedback,
        examinerFeedback: example.evaluation.examiner_feedback,
        strengths: example.evaluation.strengths || [],
        areasForImprovement: example.evaluation.areas_for_improvement || []
      }
    };
    setCurrentExample(converted);
    setOpenDialog(true);
  };

  const handleView = (example) => {
    setCurrentExample(example);
    setOpenViewDialog(true);
  };

  const handleSave = async () => {
    try {
      if (!currentExample.text || !currentExample.text.trim()) {
        showMessage('error', 'Text cannot be empty');
        return;
      }

      setLoading(true);
      
      // Prepare data for backend
      const payload = {
        text: currentExample.text,
        evaluation: currentExample.evaluation,
        metadata: currentExample.metadata
      };

      if (currentExample.id) {
        // Update
        await axios.put(`/admin/rag-examples/${currentExample.id}`, payload);
        showMessage('success', 'Example updated successfully');
      } else {
        // Create
        await axios.post('/admin/rag-examples', payload);
        showMessage('success', 'Example created successfully');
      }
      
      setOpenDialog(false);
      fetchExamples();
      fetchStats();
    } catch (error) {
      console.error('Error saving example:', error);
      showMessage('error', error.response?.data?.message || 'Failed to save example');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this example? This will trigger AI service reload.')) {
      return;
    }

    try {
      setLoading(true);
      await axios.delete(`/admin/rag-examples/${id}`);
      showMessage('success', 'Example deleted successfully');
      fetchExamples();
      fetchStats();
    } catch (error) {
      console.error('Error deleting example:', error);
      showMessage('error', 'Failed to delete example');
    } finally {
      setLoading(false);
    }
  };

  const handleReload = async () => {
    if (!window.confirm('This will reload the AI service and rebuild FAISS index. Continue?')) {
      return;
    }

    try {
      setLoading(true);
      await axios.post('/admin/rag-examples/reload');
      showMessage('success', 'AI service reloaded successfully');
      fetchStats();
    } catch (error) {
      console.error('Error reloading AI service:', error);
      showMessage('error', 'Failed to reload AI service');
    } finally {
      setLoading(false);
    }
  };

  const getBandColor = (band) => {
    if (band >= 7.0) return 'success';
    if (band >= 6.0) return 'primary';
    if (band >= 5.0) return 'warning';
    return 'error';
  };

  return (
    <Box p={3}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">RAG Examples Management</Typography>
        <Box>
          <Button
            variant="contained"
            color="primary"
            startIcon={<Add />}
            onClick={handleCreate}
            sx={{ mr: 1 }}
            disabled={loading}
          >
            Add Example
          </Button>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={handleReload}
            disabled={loading}
          >
            Reload AI Service
          </Button>
        </Box>
      </Box>

      {/* Statistics Cards */}
      {stats && (
        <Grid container spacing={2} mb={3}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography color="textSecondary" gutterBottom>Total Examples</Typography>
                <Typography variant="h3">{stats.total}</Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography color="textSecondary" gutterBottom>Speaking Examples</Typography>
                <Typography variant="h3">{stats.speaking?.total || 0}</Typography>
                <Typography variant="body2" color="textSecondary">
                  {stats.speaking?.byPart && Object.keys(stats.speaking.byPart).length > 0 
                    ? Object.entries(stats.speaking.byPart).map(([part, count]) => 
                        `${part}: ${count}`
                      ).join(', ')
                    : 'No data'}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography color="textSecondary" gutterBottom>Writing Examples</Typography>
                <Typography variant="h3">{stats.writing?.total || 0}</Typography>
                <Typography variant="body2" color="textSecondary">
                  {stats.writing?.byTask && Object.keys(stats.writing.byTask).length > 0
                    ? Object.entries(stats.writing.byTask).map(([task, count]) => 
                        `${task}: ${count}`
                      ).join(', ')
                    : 'No data'}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Type</InputLabel>
              <Select 
                value={filterType} 
                label="Type"
                onChange={(e) => setFilterType(e.target.value)}
              >
                <MenuItem value="all">All Types</MenuItem>
                <MenuItem value="speaking">Speaking</MenuItem>
                <MenuItem value="writing">Writing</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Band Score</InputLabel>
              <Select 
                value={filterBand} 
                label="Band Score"
                onChange={(e) => setFilterBand(e.target.value)}
              >
                <MenuItem value="all">All Bands</MenuItem>
                {[3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0].map(band => (
                  <MenuItem key={band} value={band}>Band {band}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth size="small">
              <InputLabel>Task/Part</InputLabel>
              <Select 
                value={filterTask} 
                label="Task/Part"
                onChange={(e) => setFilterTask(e.target.value)}
              >
                <MenuItem value="all">All</MenuItem>
                <MenuItem value="task1">Task 1</MenuItem>
                <MenuItem value="task2">Task 2</MenuItem>
                <MenuItem value="part1">Part 1</MenuItem>
                <MenuItem value="part2">Part 2</MenuItem>
                <MenuItem value="part3">Part 3</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Examples Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell><strong>ID</strong></TableCell>
              <TableCell><strong>Type</strong></TableCell>
              <TableCell><strong>Band</strong></TableCell>
              <TableCell><strong>Task/Part</strong></TableCell>
              <TableCell><strong>Text Preview</strong></TableCell>
              <TableCell align="right"><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading && examples.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : examples.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  <Typography color="textSecondary">No examples found</Typography>
                </TableCell>
              </TableRow>
            ) : (
              examples.map((example) => (
                <TableRow key={example.id} hover>
                  <TableCell>{example.id}</TableCell>
                  <TableCell>
                    <Chip 
                      label={example.metadata.type} 
                      size="small"
                      color={example.metadata.type === 'speaking' ? 'info' : 'secondary'}
                    />
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={`Band ${example.metadata.band}`} 
                      color={getBandColor(example.metadata.band)} 
                      size="small" 
                    />
                  </TableCell>
                  <TableCell>
                    {example.metadata.task || example.metadata.part || example.metadata.parts?.join(', ') || '-'}
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" noWrap sx={{ maxWidth: 400 }}>
                      {example.text.substring(0, 100)}...
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <IconButton onClick={() => handleView(example)} size="small" color="info">
                      <Visibility />
                    </IconButton>
                    <IconButton onClick={() => handleEdit(example)} size="small" color="primary">
                      <Edit />
                    </IconButton>
                    <IconButton onClick={() => handleDelete(example.id)} size="small" color="error">
                      <Delete />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Edit/Create Dialog */}
      <ExampleDialog
        open={openDialog}
        example={currentExample}
        onClose={() => setOpenDialog(false)}
        onSave={handleSave}
        onChange={setCurrentExample}
        loading={loading}
      />

      {/* View Dialog */}
      <ViewDialog
        open={openViewDialog}
        example={currentExample}
        onClose={() => setOpenViewDialog(false)}
      />

      {/* Snackbar for messages */}
      <Snackbar
        open={!!message}
        autoHideDuration={4000}
        onClose={() => setMessage(null)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        {message && (
          <Alert 
            onClose={() => setMessage(null)} 
            severity={message.type}
            sx={{ width: '100%' }}
          >
            {message.text}
          </Alert>
        )}
      </Snackbar>
    </Box>
  );
}

// Separate dialog components for better organization
function ExampleDialog({ open, example, onClose, onSave, onChange, loading }) {
  const [advancedMode, setAdvancedMode] = useState(false);
  
  if (!example) return null;

  const handleTextChange = (e) => {
    onChange({ ...example, text: e.target.value });
  };

  const handleMetadataChange = (field, value) => {
    const updates = {
      ...example,
      metadata: { ...example.metadata, [field]: value }
    };
    
    // Sync overall_band with metadata.band
    if (field === 'band') {
      updates.evaluation = {
        ...example.evaluation,
        overallBand: value
      };
    }
    
    onChange(updates);
  };

  const handleEvaluationChange = (field, value) => {
    onChange({
      ...example,
      evaluation: { ...example.evaluation, [field]: value }
    });
  };

  const handleCriteriaScoreChange = (criterion, value) => {
    onChange({
      ...example,
      evaluation: {
        ...example.evaluation,
        criteriaScores: {
          ...example.evaluation.criteriaScores,
          [criterion]: value
        }
      }
    });
  };

  const handleStrengthChange = (index, value) => {
    const newStrengths = [...(example.evaluation.strengths || [])];
    newStrengths[index] = value;
    handleEvaluationChange('strengths', newStrengths);
  };

  const addStrength = () => {
    const strengths = [...(example.evaluation.strengths || []), ''];
    handleEvaluationChange('strengths', strengths);
  };

  const removeStrength = (index) => {
    const strengths = [...(example.evaluation.strengths || [])];
    strengths.splice(index, 1);
    handleEvaluationChange('strengths', strengths);
  };

  const handleAreaChange = (index, value) => {
    const newAreas = [...(example.evaluation.areasForImprovement || [])];
    newAreas[index] = value;
    handleEvaluationChange('areasForImprovement', newAreas);
  };

  const addArea = () => {
    const areas = [...(example.evaluation.areasForImprovement || []), ''];
    handleEvaluationChange('areasForImprovement', areas);
  };

  const removeArea = (index) => {
    const areas = [...(example.evaluation.areasForImprovement || [])];
    areas.splice(index, 1);
    handleEvaluationChange('areasForImprovement', areas);
  };

  const getCriteriaLabels = () => {
    return [
      { key: 'fluency_coherence', label: 'Fluency & Coherence' },
      { key: 'lexical_resource', label: 'Lexical Resource' },
      { key: 'grammatical_range_accuracy', label: 'Grammatical Range & Accuracy' },
      { key: 'pronunciation', label: 'Pronunciation' }
    ];
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <span>{example.id ? 'Edit Example' : 'Create New Example'}</span>
          <FormControlLabel
            control={
              <Switch
                checked={advancedMode}
                onChange={(e) => setAdvancedMode(e.target.checked)}
                color="primary"
              />
            }
            label={
              <Box display="flex" alignItems="center" gap={1}>
                <span>Advanced Mode</span>
                {!advancedMode && <Chip label="Auto-generate" size="small" color="info" />}
              </Box>
            }
          />
        </Box>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ pt: 2 }}>
          <Box sx={{ mt: 2 }}>
            {/* Metadata */}
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <FormControl fullWidth>
                  <InputLabel>Type *</InputLabel>
                  <Select
                    value={example.metadata.type}
                    label="Type *"
                    onChange={(e) => handleMetadataChange('type', e.target.value)}
                  >
                    <MenuItem value="speaking">Speaking</MenuItem>
                    <MenuItem value="writing">Writing</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="Band Score *"
                  type="number"
                  inputProps={{ min: 0, max: 9, step: 0.5 }}
                  value={example.metadata.band}
                  onChange={(e) => handleMetadataChange('band', parseFloat(e.target.value))}
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                {example.metadata.type === 'writing' ? (
                  <FormControl fullWidth>
                    <InputLabel>Task</InputLabel>
                    <Select
                      value={example.metadata.task || ''}
                      label="Task"
                      onChange={(e) => handleMetadataChange('task', e.target.value)}
                    >
                      <MenuItem value="task1">Task 1</MenuItem>
                      <MenuItem value="task2">Task 2</MenuItem>
                    </Select>
                  </FormControl>
                ) : (
                  <TextField
                    fullWidth
                    label="Part (e.g., part1, part2)"
                    value={example.metadata.part || ''}
                    onChange={(e) => handleMetadataChange('part', e.target.value)}
                  />
                )}
              </Grid>
            </Grid>

            {/* Text */}
            <TextField
              fullWidth
              label="Example Text *"
              multiline
              rows={6}
              value={example.text}
              onChange={handleTextChange}
              sx={{ mt: 2 }}
              placeholder="Enter the question and answer text..."
            />

            {/* Examiner Feedback - Always visible */}
            <TextField
              fullWidth
              label="Examiner Feedback *"
              multiline
              rows={3}
              value={example.evaluation.examinerFeedback || ''}
              onChange={(e) => handleEvaluationChange('examinerFeedback', e.target.value)}
              sx={{ mt: 2 }}
              placeholder="Enter overall feedback in Vietnamese..."
            />

            {/* Advanced Mode Fields */}
            {advancedMode && (
              <>
                <Divider sx={{ my: 3 }}>
                  <Chip label="Advanced Fields" color="primary" />
                </Divider>

                {/* Criteria Scores */}
                <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 'bold' }}>
                  Criteria Scores (0-9)
                </Typography>
                <Grid container spacing={2}>
                  {getCriteriaLabels().map(({ key, label }) => (
                    <Grid item xs={12} sm={6} key={key}>
                      <Typography variant="caption" color="textSecondary">
                        {label}
                      </Typography>
                      <Slider
                        value={example.evaluation.criteriaScores?.[key] || 5.0}
                        onChange={(e, value) => handleCriteriaScoreChange(key, value)}
                        min={0}
                        max={9}
                        step={0.5}
                        marks
                        valueLabelDisplay="auto"
                      />
                    </Grid>
                  ))}
                </Grid>

                {/* Detailed Feedback */}
                <TextField
                  fullWidth
                  label="Detailed Feedback"
                  multiline
                  rows={4}
                  value={example.evaluation.detailedFeedback || ''}
                  onChange={(e) => handleEvaluationChange('detailedFeedback', e.target.value)}
                  sx={{ mt: 3 }}
                  placeholder="Enter detailed feedback for each criterion..."
                />

                {/* Strengths */}
                <Box sx={{ mt: 3 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="subtitle1" fontWeight="bold">Strengths</Typography>
                    <Button
                      size="small"
                      startIcon={<AddIcon />}
                      onClick={addStrength}
                    >
                      Add
                    </Button>
                  </Box>
                  {(example.evaluation.strengths || []).map((strength, index) => (
                    <Box key={index} display="flex" gap={1} mb={1}>
                      <TextField
                        fullWidth
                        size="small"
                        value={strength}
                        onChange={(e) => handleStrengthChange(index, e.target.value)}
                        placeholder="Enter a strength..."
                      />
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => removeStrength(index)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Box>
                  ))}
                  {(!example.evaluation.strengths || example.evaluation.strengths.length === 0) && (
                    <Typography variant="body2" color="textSecondary" sx={{ fontStyle: 'italic' }}>
                      Leave empty for auto-generation based on band score
                    </Typography>
                  )}
                </Box>

                {/* Areas for Improvement */}
                <Box sx={{ mt: 3 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="subtitle1" fontWeight="bold">Areas for Improvement</Typography>
                    <Button
                      size="small"
                      startIcon={<AddIcon />}
                      onClick={addArea}
                    >
                      Add
                    </Button>
                  </Box>
                  {(example.evaluation.areasForImprovement || []).map((area, index) => (
                    <Box key={index} display="flex" gap={1} mb={1}>
                      <TextField
                        fullWidth
                        size="small"
                        value={area}
                        onChange={(e) => handleAreaChange(index, e.target.value)}
                        placeholder="Enter an area for improvement..."
                      />
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => removeArea(index)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Box>
                  ))}
                  {(!example.evaluation.areasForImprovement || example.evaluation.areasForImprovement.length === 0) && (
                    <Typography variant="body2" color="textSecondary" sx={{ fontStyle: 'italic' }}>
                      Leave empty for auto-generation based on band score
                    </Typography>
                  )}
                </Box>
              </>
            )}
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
        <Button 
          onClick={onSave} 
          variant="contained" 
          disabled={loading || !example.text}
        >
          {loading ? <CircularProgress size={24} /> : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

ExampleDialog.propTypes = {
  open: PropTypes.bool.isRequired,
  example: PropTypes.shape({
    id: PropTypes.string,
    text: PropTypes.string,
    evaluation: PropTypes.shape({
      overallBand: PropTypes.number,
      criteriaScores: PropTypes.object,
      detailedFeedback: PropTypes.string,
      examinerFeedback: PropTypes.string,
      strengths: PropTypes.arrayOf(PropTypes.string),
      areasForImprovement: PropTypes.arrayOf(PropTypes.string)
    }),
    metadata: PropTypes.shape({
      type: PropTypes.string,
      band: PropTypes.number,
      task: PropTypes.string,
      part: PropTypes.string
    })
  }),
  onClose: PropTypes.func.isRequired,
  onSave: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  loading: PropTypes.bool
};

function ViewDialog({ open, example, onClose }) {
  if (!example) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>View Example: {example.id}</DialogTitle>
      <DialogContent>
        <Box sx={{ pt: 2 }}>
          <Typography variant="subtitle2" color="textSecondary">Type</Typography>
          <Chip label={example.metadata.type} sx={{ mb: 2 }} />

          <Typography variant="subtitle2" color="textSecondary">Band Score</Typography>
          <Chip label={`Band ${example.metadata.band}`} color="primary" sx={{ mb: 2 }} />

          <Typography variant="subtitle2" color="textSecondary">Text</Typography>
          <Paper sx={{ p: 2, mb: 2, bgcolor: 'grey.50' }}>
            <Typography variant="body2" style={{ whiteSpace: 'pre-wrap' }}>
              {example.text}
            </Typography>
          </Paper>

          {example.evaluation.examiner_feedback && (
            <>
              <Typography variant="subtitle2" color="textSecondary">Examiner Feedback</Typography>
              <Paper sx={{ p: 2, bgcolor: 'grey.50' }}>
                <Typography variant="body2">
                  {example.evaluation.examiner_feedback}
                </Typography>
              </Paper>
            </>
          )}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}

ViewDialog.propTypes = {
  open: PropTypes.bool.isRequired,
  example: PropTypes.shape({
    id: PropTypes.string,
    text: PropTypes.string,
    evaluation: PropTypes.shape({
      examiner_feedback: PropTypes.string
    }),
    metadata: PropTypes.shape({
      type: PropTypes.string,
      band: PropTypes.number
    })
  }),
  onClose: PropTypes.func.isRequired
};
